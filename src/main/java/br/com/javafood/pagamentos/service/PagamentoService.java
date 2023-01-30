package br.com.javafood.pagamentos.service;

import br.com.javafood.pagamentos.dto.PagamentoDto;
import br.com.javafood.pagamentos.model.Pagamento;
import br.com.javafood.pagamentos.model.Status;
import br.com.javafood.pagamentos.repository.PagamentoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
public class PagamentoService {
    @Autowired
    private PagamentoRepository _repository;
    @Autowired
    private ModelMapper _modelMapper;

    public Page<PagamentoDto> getAll(Pageable pagination){
        return _repository
                .findAll(pagination)
                .map(pagamento -> _modelMapper.map(pagamento, PagamentoDto.class));
    }

    public PagamentoDto getById(Long id){
        Pagamento pagamento = _repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException());
        return _modelMapper.map(pagamento, PagamentoDto.class);
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

}
