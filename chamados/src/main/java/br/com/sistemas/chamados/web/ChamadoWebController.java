package br.com.sistemas.chamados.web;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.dto.AlterarStatusRequest;
import br.com.sistemas.chamados.dto.AtribuirTecnicoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.exception.ChamadoFechadoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.service.ComentarioService;
import br.com.sistemas.chamados.service.TecnicoService;
import br.com.sistemas.chamados.web.form.ChamadoForm;
import br.com.sistemas.chamados.web.form.ComentarioForm;
import jakarta.validation.Valid;

/**
 * Telas de chamado. Usa os mesmos services da API (/chamados), inclusive
 * filtros + paginacao, atribuicao de tecnico, mudanca de status e comentarios.
 */
@Controller
@RequestMapping("/ui/chamados")
public class ChamadoWebController {

    private static final String FORM = "chamados/form";
    private static final String DETALHE = "chamados/detalhe";
    private static final int TAMANHO_PAGINA = 10;

    private final ChamadoService service;
    private final ClienteService clienteService;
    private final CategoriaService categoriaService;
    private final TecnicoService tecnicoService;
    private final ComentarioService comentarioService;

    public ChamadoWebController(ChamadoService service, ClienteService clienteService,
                                CategoriaService categoriaService, TecnicoService tecnicoService,
                                ComentarioService comentarioService) {
        this.service = service;
        this.clienteService = clienteService;
        this.categoriaService = categoriaService;
        this.tecnicoService = tecnicoService;
        this.comentarioService = comentarioService;
    }

    /* ---------- lista com filtros e paginacao ---------- */

    @GetMapping
    public String listar(@RequestParam(required = false) StatusChamado status,
                         @RequestParam(required = false) Prioridade prioridade,
                         @RequestParam(required = false) Long clienteId,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        Page<ChamadoResponse> resultado = service.listar(status, prioridade, clienteId,
                PageRequest.of(Math.max(page, 0), TAMANHO_PAGINA,
                        Sort.by(Sort.Direction.DESC, "dataAbertura")));
        model.addAttribute("pagina", resultado);
        model.addAttribute("statusSel", status);
        model.addAttribute("prioridadeSel", prioridade);
        model.addAttribute("clienteSel", clienteId);
        model.addAttribute("statusLista", StatusChamado.values());
        model.addAttribute("prioridadeLista", Prioridade.values());
        model.addAttribute("clientes", clienteService.listar());
        return "chamados/lista";
    }

    /* ---------- novo / criar ---------- */

    private void opcoesForm(Model model, String modo, Long id) {
        model.addAttribute("modo", modo);
        model.addAttribute("id", id);
        model.addAttribute("clientes", clienteService.listar());
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("prioridades", Prioridade.values());
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long clienteId, Model model) {
        ChamadoForm form = new ChamadoForm();
        form.setPrioridade(Prioridade.MEDIA);
        form.setClienteId(clienteId);
        model.addAttribute("form", form);
        opcoesForm(model, "novo", null);
        return FORM;
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") ChamadoForm form,
                        BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            opcoesForm(model, "novo", null);
            return FORM;
        }
        ChamadoResponse criado = service.criar(form.paraRequest());
        flash.addFlashAttribute("sucesso", "Chamado aberto.");
        return "redirect:/ui/chamados/" + criado.id();
    }

    /* ---------- detalhe, comentarios, atribuir, status ---------- */

    private void carregarDetalhe(Model model, Long id) {
        model.addAttribute("chamado", service.buscar(id));
        model.addAttribute("comentarios", comentarioService.listar(id));
        model.addAttribute("tecnicos", tecnicoService.listar());
        model.addAttribute("statusOpcoes", List.of(StatusChamado.EM_ATENDIMENTO,
                StatusChamado.AGUARDANDO_CLIENTE, StatusChamado.FECHADO));
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        carregarDetalhe(model, id);
        if (!model.containsAttribute("comentarioForm")) {
            model.addAttribute("comentarioForm", new ComentarioForm());
        }
        return DETALHE;
    }

    @PostMapping("/{id}/comentarios")
    public String comentar(@PathVariable Long id,
                           @Valid @ModelAttribute("comentarioForm") ComentarioForm form,
                           BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            carregarDetalhe(model, id);
            return DETALHE;
        }
        comentarioService.criar(id, form.paraRequest());
        flash.addFlashAttribute("sucesso", "Comentário registrado.");
        return "redirect:/ui/chamados/" + id;
    }

    @PostMapping("/{id}/atribuir")
    public String atribuir(@PathVariable Long id,
                           @RequestParam(required = false) Long tecnicoId,
                           RedirectAttributes flash) {
        if (tecnicoId == null) {
            flash.addFlashAttribute("erro", "Selecione um técnico.");
            return "redirect:/ui/chamados/" + id;
        }
        try {
            service.atribuir(id, new AtribuirTecnicoRequest(tecnicoId));
            flash.addFlashAttribute("sucesso", "Técnico atribuído.");
        } catch (ChamadoFechadoException | RegraNegocioException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/ui/chamados/" + id;
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id,
                                @RequestParam(required = false) StatusChamado status,
                                RedirectAttributes flash) {
        if (status == null) {
            flash.addFlashAttribute("erro", "Selecione o novo status.");
            return "redirect:/ui/chamados/" + id;
        }
        try {
            service.alterarStatus(id, new AlterarStatusRequest(status));
            flash.addFlashAttribute("sucesso", "Status atualizado.");
        } catch (ChamadoFechadoException | RegraNegocioException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/ui/chamados/" + id;
    }

    /* ---------- editar / excluir ---------- */

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        ChamadoResponse atual = service.buscar(id);
        if (atual.status() == StatusChamado.FECHADO) {
            flash.addFlashAttribute("erro", "Chamado fechado não pode ser alterado.");
            return "redirect:/ui/chamados/" + id;
        }
        model.addAttribute("form", ChamadoForm.de(atual));
        opcoesForm(model, "editar", id);
        return FORM;
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") ChamadoForm form,
                            BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            opcoesForm(model, "editar", id);
            return FORM;
        }
        try {
            service.atualizar(id, form.paraRequest());
        } catch (ChamadoFechadoException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/ui/chamados/" + id;
        }
        flash.addFlashAttribute("sucesso", "Alterações salvas.");
        return "redirect:/ui/chamados/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes flash) {
        try {
            service.excluir(id);
        } catch (DataIntegrityViolationException ex) {
            flash.addFlashAttribute("erro", "Não é possível excluir: este chamado tem comentários.");
            return "redirect:/ui/chamados/" + id;
        }
        flash.addFlashAttribute("sucesso", "Chamado excluído.");
        return "redirect:/ui/chamados";
    }
}
