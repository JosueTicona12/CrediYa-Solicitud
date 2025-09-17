package co.com.solicitud.usecase.capacidad;

import co.com.solicitud.model.capacidad.CapacidadEndeudamientoRequest;
import co.com.solicitud.model.capacidad.CapacidadEndeudamientoResponse;
import co.com.solicitud.model.capacidad.PrestamoActivo;
import co.com.solicitud.usecase.capacidad.utils.CapacidadLogEnum;
import co.com.solicitud.usecase.solicitud.utils.SolicitudErrorEnum;
import exceptions.SolicitudValidationException;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Log
public class CapacidadUseCase {
    private static final BigDecimal FACTOR_POLITICA_RIESGO = new BigDecimal("0.35");
    private static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);
    private static final int SCALE = 2;

    public Mono<CapacidadEndeudamientoResponse> calcularCapacidad(CapacidadEndeudamientoRequest request) {
        return Mono.fromCallable(() -> realizarCalculo(request))
                .doOnSubscribe(subscription -> log.fine(CapacidadLogEnum.CALCULO_INICIADO.message()))
                .doOnSuccess(response -> log.info(CapacidadLogEnum.CALCULO_COMPLETADO.message()))
                .doOnError(error -> log.severe(CapacidadLogEnum.ERROR_CALCULO.message() + error.getMessage()));
    }

    private CapacidadEndeudamientoResponse realizarCalculo(CapacidadEndeudamientoRequest request) {
        if (request == null) {
            throw new SolicitudValidationException(SolicitudErrorEnum.CAPACIDAD_REQUEST_NULO.message());
        }

        BigDecimal ingresosTotales = Optional.ofNullable(request.getIngresosTotales())
                .filter(valor -> valor.compareTo(BigDecimal.ZERO) > 0)
                .orElseThrow(() -> new SolicitudValidationException(SolicitudErrorEnum.CAPACIDAD_INGRESOS_INVALIDOS.message()));

        BigDecimal montoNuevo = Optional.ofNullable(request.getMontoNuevo())
                .filter(valor -> valor.compareTo(BigDecimal.ZERO) > 0)
                .orElseThrow(() -> new SolicitudValidationException(SolicitudErrorEnum.CAPACIDAD_MONTO_NUEVO_INVALIDO.message()));

        BigDecimal tasaNueva = Optional.ofNullable(request.getTasaInteresMensualNueva())
                .filter(valor -> valor.compareTo(BigDecimal.ZERO) >= 0)
                .orElseThrow(() -> new SolicitudValidationException(SolicitudErrorEnum.CAPACIDAD_TASA_INVALIDA.message()));

        Integer plazoNuevo = Optional.ofNullable(request.getPlazoMesesNuevo())
                .filter(valor -> valor > 0)
                .orElseThrow(() -> new SolicitudValidationException(SolicitudErrorEnum.CAPACIDAD_PLAZO_INVALIDO.message()));

        List<PrestamoActivo> prestamosActivos = Optional.ofNullable(request.getPrestamosActivos())
                .orElseGet(List::of);

        BigDecimal capacidadMaxima = ingresosTotales
                .multiply(FACTOR_POLITICA_RIESGO, MATH_CONTEXT)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal deudaMensualActual = prestamosActivos.stream()
                .map(this::calcularCuotaPrestamoActivo)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal capacidadDisponible = capacidadMaxima
                .subtract(deudaMensualActual, MATH_CONTEXT)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal cuotaNuevoPrestamo = calcularCuota(montoNuevo, tasaNueva, plazoNuevo)
                .setScale(SCALE, RoundingMode.HALF_UP);

        boolean aprobado = capacidadDisponible.compareTo(cuotaNuevoPrestamo) >= 0;
        String decision = aprobado ? "APROBADO" : "RECHAZADO";

        return CapacidadEndeudamientoResponse.builder()
                .capacidadEndeudamientoMaxima(capacidadMaxima)
                .deudaMensualActual(deudaMensualActual)
                .capacidadEndeudamientoDisponible(capacidadDisponible)
                .cuotaPrestamoNuevo(cuotaNuevoPrestamo)
                .aprobado(aprobado)
                .decision(decision)
                .build();
    }

    private BigDecimal calcularCuotaPrestamoActivo(PrestamoActivo prestamo) {
        if (prestamo == null) {
            throw new SolicitudValidationException(SolicitudErrorEnum.CAPACIDAD_PRESTAMO_INVALIDO.message());
        }
        BigDecimal monto = Optional.ofNullable(prestamo.getMonto())
                .filter(valor -> valor.compareTo(BigDecimal.ZERO) > 0)
                .orElseThrow(() -> new SolicitudValidationException(SolicitudErrorEnum.CAPACIDAD_PRESTAMO_INVALIDO.message()));

        BigDecimal tasa = Optional.ofNullable(prestamo.getTasaInteresMensual())
                .filter(valor -> valor.compareTo(BigDecimal.ZERO) >= 0)
                .orElseThrow(() -> new SolicitudValidationException(SolicitudErrorEnum.CAPACIDAD_PRESTAMO_INVALIDO.message()));

        Integer plazo = Optional.ofNullable(prestamo.getPlazoMeses())
                .filter(valor -> valor > 0)
                .orElseThrow(() -> new SolicitudValidationException(SolicitudErrorEnum.CAPACIDAD_PRESTAMO_INVALIDO.message()));

        return calcularCuota(monto, tasa, plazo);
    }

    private BigDecimal calcularCuota(BigDecimal monto, BigDecimal tasaInteresMensual, int plazoMeses) {
        if (plazoMeses <= 0) {
            throw new SolicitudValidationException(SolicitudErrorEnum.CAPACIDAD_PLAZO_INVALIDO.message());
        }

        if (tasaInteresMensual.compareTo(BigDecimal.ZERO) == 0) {
            return monto.divide(BigDecimal.valueOf(plazoMeses), SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal unoMasTasa = BigDecimal.ONE.add(tasaInteresMensual, MATH_CONTEXT);
        BigDecimal potencia = unoMasTasa.pow(plazoMeses, MATH_CONTEXT);
        BigDecimal numerador = monto.multiply(tasaInteresMensual, MATH_CONTEXT).multiply(potencia, MATH_CONTEXT);
        BigDecimal denominador = potencia.subtract(BigDecimal.ONE, MATH_CONTEXT);

        if (denominador.compareTo(BigDecimal.ZERO) == 0) {
            return monto.divide(BigDecimal.valueOf(plazoMeses), SCALE, RoundingMode.HALF_UP);
        }

        return numerador.divide(denominador, SCALE, RoundingMode.HALF_UP);
    }
}
