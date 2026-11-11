package br.com.sistemas.chamados.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.TecnicoService;
import br.com.sistemas.chamados.web.form.TecnicoForm;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/ui/tecnicos")
public class TecnicoWebController {

    private static final String FORM = "tecnicos/form";

    private final TecnicoService service;

    public TecnicoWebController(TecnicoService service) {
        this.service = service;
    }

    private void opcoes(Model model, String modo, Long id) {
        model.addAttribute("modo", modo);
        model.addAttribute("id", id);
        model.addAttribute("perfis", Perfil.values());
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tecnicos", service.listar());
        return "tecnicos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new TecnicoForm());
        opcoes(model, "novo", null);
        return FORM;
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") TecnicoForm form,
                        BindingResult result, Model model, RedirectAttributes flash) {
        opcoes(model, "novo", null);
        if (result.hasErrors()) {
            return FORM;
        }
        try {
            service.criar(form.paraRequest());
        } catch (RegraNegocioException ex) {
            result.rejectValue("email", "duplicado", ex.getMessage());
            return FORM;
        }
        flash.addFlashAttribute("sucesso", "Técnico cadastrado.");
        return "redirect:/ui/tecnicos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", TecnicoForm.de(service.buscar(id)));
        opcoes(model, "editar", id);
        return FORM;
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") TecnicoForm form,
                            BindingResult result, Model model, RedirectAttributes flash) {
        opcoes(model, "editar", id);
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
        return "redirect:/ui/tecnicos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes flash) {
        try {
            service.excluir(id);
        } catch (DataIntegrityViolationException ex) {
            flash.addFlashAttribute("erro", "Não é possível excluir: este técnico está atribuído a chamados.");
            return "redirect:/ui/tecnicos";
        }
        flash.addFlashAttribute("sucesso", "Técnico excluído.");
        return "redirect:/ui/tecnicos";
    }
}
