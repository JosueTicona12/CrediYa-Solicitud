package co.com.solicitud.api;

import co.com.solicitud.api.config.SolicitudPath;
import co.com.solicitud.model.solicitud.Solicitud;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final SolicitudPath solicitudPath;
    private final Handler solicitudHandler;

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/solicitudes",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "listenSaveSolicitud",
                    operation = @Operation(
                            operationId = "saveSolicitud",
                            summary = "Crea una nueva solicitud",
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Datos de la solicitud a crear",
                                    content = @Content(schema = @Schema(implementation = Solicitud.class))
                            ),
                            responses = @ApiResponse(
                                    responseCode = "200",
                                    description = "Solicitud creada",
                                    content = @Content(schema = @Schema(implementation = Solicitud.class))
                            )
                    )
            ),
            @RouterOperation(
                    path = "/solicitudes/{id}",
                    method = RequestMethod.PUT,
                    beanClass = Handler.class,
                    beanMethod = "listenUpdateSolicitud",
                    operation = @Operation(
                            operationId = "updateSolicitud",
                            summary = "Actualiza una solicitud existente",
                            parameters = {
                                    @Parameter(
                                            name = "id",
                                            description = "ID de la solicitud a actualizar",
                                            required = true,
                                            in = ParameterIn.PATH,
                                            schema = @Schema(type = "long")
                                    )
                            },
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Datos actualizados de la solicitud",
                                    content = @Content(schema = @Schema(implementation = Solicitud.class))
                            ),
                            responses = @ApiResponse(
                                    responseCode = "200",
                                    description = "Solicitud actualizada",
                                    content = @Content(schema = @Schema(implementation = Solicitud.class))
                            )
                    )
            ),
            @RouterOperation(
                    path = "/solicitudes/{id}",
                    method = RequestMethod.DELETE,
                    beanClass = Handler.class,
                    beanMethod = "listenDeleteSolicitud",
                    operation = @Operation(
                            operationId = "deleteSolicitud",
                            summary = "Elimina una solicitud por id",
                            parameters = {
                                    @Parameter(
                                            name = "id",
                                            description = "ID de la solicitud a eliminar",
                                            required = true,
                                            in = ParameterIn.PATH,
                                            schema = @Schema(type = "long")
                                    )
                            },
                            responses = @ApiResponse(responseCode = "204", description = "Solicitud eliminada")
                    )
            ),
            @RouterOperation(
                    path = "/solicitudes",
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "listenGetAllSolicitud",
                    operation = @Operation(
                            operationId = "getAllSolicitud",
                            summary = "Obtiene todas las solicitudes",
                            responses = @ApiResponse(
                                    responseCode = "200",
                                    description = "Lista de solicitudes",
                                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Solicitud.class)))
                            )
                    )
            ),
            @RouterOperation(
                    path = "/solicitudes/revision",
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "listenGetSolicitudesRevision",
                    operation = @Operation(
                            operationId = "getSolicitudesRevision",
                            summary = "Obtiene solicitudes pendientes de revisión",
                            parameters = {
                                    @Parameter(name = "page", in = ParameterIn.QUERY, schema = @Schema(type = "int")),
                                    @Parameter(name = "size", in = ParameterIn.QUERY, schema = @Schema(type = "int")),
                                    @Parameter(name = "filtro", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
                            },
                            responses = @ApiResponse(
                                    responseCode = "200",
                                    description = "Lista de solicitudes para revisión",
                                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Solicitud.class)))
                            )
                    )
            ),
            @RouterOperation(
                    path = "/solicitudes/{id}",
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "listenSolicitudById",
                    operation = @Operation(
                            operationId = "getSolicitudById",
                            summary = "Obtiene una solicitud por id",
                            parameters = {
                                    @Parameter(
                                            name = "id",
                                            description = "ID de la solicitud a consultar",
                                            required = true,
                                            in = ParameterIn.PATH,
                                            schema = @Schema(type = "long")
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Solicitud encontrada",
                                            content = @Content(schema = @Schema(implementation = Solicitud.class))
                                    ),
                                    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
                            }
                    )
            )
    })

    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST(solicitudPath.getSolicitudes()), solicitudHandler::listenSaveSolicitud)
                .andRoute(PUT(solicitudPath.getSolicitudesById()), solicitudHandler::listenUpdateSolicitud)
                .andRoute(DELETE(solicitudPath.getSolicitudesById()), solicitudHandler::listenDeleteSolicitud)
                .andRoute(GET(solicitudPath.getSolicitudes()), solicitudHandler::listenGetAllSolicitud)
                .andRoute(GET(solicitudPath.getSolicitudesRevision()), solicitudHandler::listenGetSolicitudesRevision)
                .andRoute(GET(solicitudPath.getSolicitudesById()), solicitudHandler::listenSolicitudById);
    }
}
