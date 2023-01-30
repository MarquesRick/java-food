package br.com.javafood.pagamentos.repository;

import br.com.javafood.pagamentos.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

// JPA for basic crud (generic repository)
// JpaRepository<TypeClassRepository, TypeIdClass>
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
}
