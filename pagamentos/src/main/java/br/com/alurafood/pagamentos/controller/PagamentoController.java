package br.com.alurafood.pagamentos.controller;

import br.com.alurafood.pagamentos.dto.PagamentoDto;
import br.com.alurafood.pagamentos.service.PagamentoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @GetMapping
    public Page<PagamentoDto> getAll(@PageableDefault(size = 10) Pageable pageable) {
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    @CircuitBreaker(name = "getPagamentoComItens", fallbackMethod = "getPagamentosSemItens")
    public ResponseEntity<PagamentoDto> getById(@PathVariable @NotNull Long id) {
        PagamentoDto dto = service.getByIdIntegraPedido(id);
        return ResponseEntity.ok(dto);
    }

    public ResponseEntity<PagamentoDto> getPagamentosSemItens(Long id, Exception e) {
        PagamentoDto dto = service.getById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<PagamentoDto> create(@RequestBody @Valid PagamentoDto dto, UriComponentsBuilder uriBuilder) throws JsonProcessingException {
        PagamentoDto pagamento = service.createPagamento(dto);
        URI uri = uriBuilder.path("/pagamentos/{id}").buildAndExpand(pagamento.getId()).toUri();

        rabbitTemplate.convertAndSend("pagamentos.ex", "", pagamento);

        return ResponseEntity.created(uri).body(pagamento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagamentoDto> update(@PathVariable @NotNull Long id, @RequestBody @Valid PagamentoDto dto) {
        PagamentoDto pagamento = service.updatePagamento(id, dto);
        return ResponseEntity.ok(pagamento);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NotNull Long id) {
        service.deletePagamento(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/confirmar")
    @CircuitBreaker(name = "atualizaPedido", fallbackMethod = "pagamentoAutorizadoComIntegracaoPendente")
    public void confirmarPagamento(@PathVariable @NotNull Long id) {
        service.confirmarPagamento(id);
    }

    public void pagamentoAutorizadoComIntegracaoPendente(Long id, Exception e) {
        service.alteraStatus(id);
    }

}
