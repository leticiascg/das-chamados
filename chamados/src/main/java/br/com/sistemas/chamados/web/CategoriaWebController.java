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

import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.web.form.CategoriaForm;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/ui/categorias")
public class CategoriaWebController {

    private static final String FORM = "categorias/form";

    private final CategoriaService service;

    public CategoriaWebController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", service.listar());
        return "categorias/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new CategoriaForm());
        model.addAttribute("modo", "novo");
        return FORM;
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") CategoriaForm form,
                        BindingResult result, Model model, RedirectAttributes flash) {
        model.addAttribute("modo", "novo");
        if (result.hasErrors()) {
            return FORM;
        }
        try {
            service.criar(form.paraRequest());
        } catch (RegraNegocioException ex) {
            result.rejectValue("nome", "duplicado", ex.getMessage());
            return FORM;
        }
        flash.addFlashAttribute("sucesso", "Categoria cadastrada.");
        return "redirect:/ui/categorias";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", CategoriaForm.de(service.buscar(id)));
        model.addAttribute("modo", "editar");
        model.addAttribute("id", id);
        return FORM;
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") CategoriaForm form,
                            BindingResult result, Model model, RedirectAttributes flash) {
        model.addAttribute("modo", "editar");
        model.addAttribute("id", id);
        if (result.hasErrors()) {
            return FORM;
        }
        try {
            service.atualizar(id, form.paraRequest());
        } catch (RegraNegocioException ex) {
            result.rejectValue("nome", "duplicado", ex.getMessage());
            return FORM;
        }
        flash.addFlashAttribute("sucesso", "Alterações salvas.");
        return "redirect:/ui/categorias";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes flash) {
        try {
            service.excluir(id);
        } catch (DataIntegrityViolationException ex) {
            flash.addFlashAttribute("erro", "Não é possível excluir: esta categoria tem chamados vinculados.");
            return "redirect:/ui/categorias";
        }
        flash.addFlashAttribute("sucesso", "Categoria excluída.");
        return "redirect:/ui/categorias";
    }
}
