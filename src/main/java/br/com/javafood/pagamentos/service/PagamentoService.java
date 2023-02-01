package br.com.javafood.pagamentos.service;

import br.com.javafood.pagamentos.dto.PagamentoDto;
import br.com.javafood.pagamentos.http.PedidoClient;
import br.com.javafood.pagamentos.model.Pagamento;
import br.com.javafood.pagamentos.model.Status;
import br.com.javafood.pagamentos.repository.PagamentoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class PagamentoService {
    @Autowired
    private PagamentoRepository _repository;
    @Autowired
    private ModelMapper _modelMapper;
    @Autowired
    private PedidoClient pedido;

    public Page<PagamentoDto> getAll(Pageable pagination){
        return _repository
                .findAll(pagination)
                .map(pagamento -> _modelMapper.map(pagamento, PagamentoDto.class));
    }

    public PagamentoDto getById(Long id){
        Pagamento pagamento = _repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException());
        var dto = _modelMapper.map(pagamento, PagamentoDto.class);
        dto.setItens(pedido.obterItensDoPedido(pagamento.getPedidoId()).getItens());
        return dto;
    }

    public PagamentoDto create(PagamentoDto dto){
        Pagamento pagamento = _modelMapper.map(dto, Pagamento.class);
        pagamento.setStatus(Status.CRIADO);
        _repository.save(pagamento);

        return _modelMapper.map(pagamento, PagamentoDto.class);
    }

    public PagamentoDto update(Long id, PagamentoDto dto){
        Pagamento pagamento = _modelMapper.map(dto, Pagamento.class);
        pagamento.setId(id);
        pagamento = _repository.save(pagamento);

        return _modelMapper.map(pagamento, PagamentoDto.class);
    }

    public void delete(Long id){
        _repository.deleteById(id);
    }

    public void confirmPayment(Long id){
        Optional<Pagamento> pagamento = _repository.findById(id);

        if(!pagamento.isPresent())
            throw new EntityNotFoundException();

        pagamento.get().setStatus(Status.CONFIRMADO);
        _repository.save(pagamento.get());
        pedido.atualizarPagamento(pagamento.get().getPedidoId());
    }

    public void changeStatus(Long id){
        Optional<Pagamento> pagamento = _repository.findById(id);

        if(!pagamento.isPresent())
            throw new EntityNotFoundException();

        pagamento.get().setStatus(Status.CONFIRMADO_SEM_INTEGRACAO);
        _repository.save(pagamento.get());
    }



}
