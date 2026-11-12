package br.com.sistemas.chamados.web;

import org.springframework.stereotype.Component;

import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;

/**
 * Textos e classes CSS dos enums para as telas.
 * Nos templates: ${@rotulos.status(c.status)} e ${@rotulos.statusClasse(c.status)}.
 */
@Component("rotulos")
public class Rotulos {

    public String status(StatusChamado s) {
        return switch (s) {
            case ABERTO -> "Aberto";
            case EM_ATENDIMENTO -> "Em atendimento";
            case AGUARDANDO_CLIENTE -> "Aguardando cliente";
            case FECHADO -> "Fechado";
        };
    }

    public String statusClasse(StatusChamado s) {
        return switch (s) {
            case ABERTO -> "st-aberto";
            case EM_ATENDIMENTO -> "st-atendimento";
            case AGUARDANDO_CLIENTE -> "st-aguardando";
            case FECHADO -> "st-fechado";
        };
    }

    public String prioridade(Prioridade p) {
        return switch (p) {
            case BAIXA -> "Baixa";
            case MEDIA -> "Média";
            case ALTA -> "Alta";
            case CRITICA -> "Crítica";
        };
    }

    public String prioridadeClasse(Prioridade p) {
        return switch (p) {
            case BAIXA -> "pr-baixa";
            case MEDIA -> "pr-media";
            case ALTA -> "pr-alta";
            case CRITICA -> "pr-critica";
        };
    }

    public String perfil(Perfil p) {
        return switch (p) {
            case ADMIN -> "Administrador";
            case TECNICO -> "Técnico";
        };
    }
}
