package br.com.javafood.pagamentos.controller;

import br.com.javafood.pagamentos.dto.PagamentoDto;
import br.com.javafood.pagamentos.model.Pagamento;
import br.com.javafood.pagamentos.service.PagamentoService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {
    @Autowired
    private PagamentoService service;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @GetMapping //if you don't pass any parameter, this method returns on get request
    public Page<PagamentoDto> get(@PageableDefault(size = 10) Pageable pagination){
        return service.getAll(pagination);
    }

    @GetMapping("/{id}")
    //@PathVariable is for indicate that id passing in uri
    //@NotNull don't allow id nullable
    public ResponseEntity<PagamentoDto> getById(@PathVariable @NotNull Long id){
        PagamentoDto dto = service.getById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<PagamentoDto> create(@RequestBody @Valid PagamentoDto dto, UriComponentsBuilder uriBuilder){
        PagamentoDto pagamento = service.create(dto);
        //returns the status created + the updated obj calling method get (/pagamentos/{id})
        URI uri = uriBuilder.path("/pagamentos/{id}").buildAndExpand(pagamento.getId()).toUri();

        Message message = new Message(("Created payment ID: " + pagamento.getId()).getBytes());
        //rabbitTemplate.send("payment_done", message);

        //convert to dto and send to rabbit
        rabbitTemplate.convertAndSend("payment_done", pagamento);
        return ResponseEntity.created(uri).body(pagamento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagamentoDto> update(@PathVariable @NotNull Long id, @RequestBody @Valid PagamentoDto dto){
        PagamentoDto updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PagamentoDto> delete(@PathVariable @NotNull Long id){
        service.delete(id);
        return ResponseEntity.noContent().build(); //returns OK but no content
    }

    @PatchMapping("/{id}/confirm")
    @CircuitBreaker(name = "atualizaPedido", fallbackMethod = "pagamentoIntegracaoPendente")
    public void confirmPayment(@PathVariable @NotNull Long id){
        service.confirmPayment(id);
    }

    public void pagamentoIntegracaoPendente(Long id, Exception e){
        service.changeStatus(id);
    }



}
