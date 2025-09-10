package co.com.solicitud.r2dbc;

import co.com.solicitud.model.solicitud.Solicitud;
import co.com.solicitud.r2dbc.Entity.SolicitudEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudReactiveRepositoryAdapterTest {

    @InjectMocks
    private SolicitudReactiveRepositoryAdapter adapter;


    @Mock
    private SolicitudReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private TransactionalOperator transactionalOperator;

   @Test
   void save_delegatesToRepository() {
       Solicitud model = new Solicitud();
       model.setEmail("test@test.com");
       SolicitudEntity entity = new SolicitudEntity();
       entity.setEmail("test@test.com");

       when(mapper.map(model, SolicitudEntity.class)).thenReturn(entity);
       when(repository.save(entity)).thenReturn(Mono.just(entity));
       when(mapper.map(entity, Solicitud.class)).thenReturn(model);
       when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

       StepVerifier.create(adapter.save(model))
               .expectNext(model)
               .verifyComplete();

       verify(repository).save(entity);
   }

   @Test
   void findByEmail_mapsEntity() {
       SolicitudEntity entity = new SolicitudEntity();
       entity.setEmail("mail@test.com");
       Solicitud model = new Solicitud();
       model.setEmail("mail@test.com");
       when(repository.findByEmail("mail@test.com")).thenReturn(Mono.just(entity));
       when(mapper.map(entity, Solicitud.class)).thenReturn(model);
       StepVerifier.create(adapter.findByEmail("mail@test.com"))
               .expectNext(model)
               .verifyComplete();

       verify(repository).findByEmail("mail@test.com");
   }

   @Test
   void findByIdestadoIn_mapsList() {
       SolicitudEntity e1 = new SolicitudEntity();
       e1.setEmail("a@test.com");
       SolicitudEntity e2 = new SolicitudEntity();
       e2.setEmail("b@test.com");
       Solicitud m1 = new Solicitud();
       m1.setEmail("a@test.com");
       Solicitud m2 = new Solicitud();
       m2.setEmail("b@test.com");

       when(repository.findByIdestadoIn(List.of(1L,2L))).thenReturn(Flux.just(e1,e2));
       when(mapper.map(e1, Solicitud.class)).thenReturn(m1);
       when(mapper.map(e2, Solicitud.class)).thenReturn(m2);

       StepVerifier.create(adapter.findByIdestadoIn(List.of(1L,2L)))
               .expectNext(m1, m2)
               .verifyComplete();

       verify(repository).findByIdestadoIn(List.of(1L,2L));
   }

}
