package br.com.sistemas.chamados.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.sistemas.chamados.entity.Cliente;

@DataJpaTest
class ClienteRepositoryTest {

    @Autowired
    ClienteRepository repository;

    @Test
    void salvaClienteEGeraId() {
        Cliente salvo = repository.save(new Cliente("Ana Souza", "ana@exemplo.com", "47999999999"));
        assertThat(salvo.getId()).isNotNull();
    }

    @Test
    void existsByEmailEncontraEmailCadastrado() {
        repository.save(new Cliente("Ana Souza", "ana@exemplo.com", "47999999999"));
        assertThat(repository.existsByEmail("ana@exemplo.com")).isTrue();
        assertThat(repository.existsByEmail("outro@exemplo.com")).isFalse();
    }

    @Test
    void emailDuplicadoViolaRestricaoUnica() {
        repository.saveAndFlush(new Cliente("Ana Souza", "ana@exemplo.com", "47999999999"));
        assertThatThrownBy(() ->
                repository.saveAndFlush(new Cliente("Outro", "ana@exemplo.com", "47988888888")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}