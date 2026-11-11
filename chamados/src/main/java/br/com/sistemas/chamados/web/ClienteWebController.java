package br.com.sistemas.chamados.web;

import org.springframework.dao.DataIntegrityViolationException;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.web.form.ClienteForm;
import jakarta.validation.Valid;

/** Telas de cliente: reaproveitam o mesmo ClienteService da API REST (/clientes). */
@Controller
@RequestMapping("/ui/clientes")
public class ClienteWebController {

    private static final String FORM = "clientes/form";

    private final ClienteService service;
    private final ChamadoService chamadoService;

    public ClienteWebController(ClienteService service, ChamadoService chamadoService) {
        this.service = service;
        this.chamadoService = chamadoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", service.listar());
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new ClienteForm());
        model.addAttribute("modo", "novo");
        return FORM;
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") ClienteForm form,
                        BindingResult result, Model model, RedirectAttributes flash) {
        model.addAttribute("modo", "novo");
        if (result.hasErrors()) {
            return FORM;
        }
        try {
            service.criar(form.paraRequest());
        } catch (RegraNegocioException ex) {
            result.rejectValue("email", "duplicado", ex.getMessage());
            return FORM;
        }
        flash.addFlashAttribute("sucesso", "Cliente cadastrado.");
        return "redirect:/ui/clientes";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", service.buscar(id));
        model.addAttribute("chamados", chamadoService.listarPorCliente(id,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dataAbertura"))).getContent());
        return "clientes/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        ClienteResponse atual = service.buscar(id);
        model.addAttribute("form", ClienteForm.de(atual));
        model.addAttribute("modo", "editar");
        model.addAttribute("id", id);
        return FORM;
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") ClienteForm form,
                            BindingResult result, Model model, RedirectAttributes flash) {
        model.addAttribute("modo", "editar");
        model.addAttribute("id", id);
        if (result.hasErrors()) {
            return FORM;
        }
        try {
            service.atualizar(id, form.paraRequest());
        } catch (RegraNegocioException ex) {
            result.rejectValue("email", "duplicado", ex.getMessage());
            return FORM;
        }
        flash.addFlashAttribute("sucesso", "Alterações salvas.");
        return "redirect:/ui/clientes/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes flash) {
        try {
            service.excluir(id);
        } catch (DataIntegrityViolationException ex) {
            flash.addFlashAttribute("erro", "Não é possível excluir: este cliente tem chamados vinculados.");
            return "redirect:/ui/clientes/" + id;
        }
        flash.addFlashAttribute("sucesso", "Cliente excluído.");
        return "redirect:/ui/clientes";
    }
}
