package br.com.sistemas.chamados.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.service.TecnicoService;

/** Controller WEB: devolve nomes de templates Thymeleaf, nao JSON. */
@Controller
public class DashboardWebController {

    private final ChamadoService chamadoService;
    private final ClienteService clienteService;
    private final TecnicoService tecnicoService;
    private final CategoriaService categoriaService;

    public DashboardWebController(ChamadoService chamadoService, ClienteService clienteService,
                                  TecnicoService tecnicoService, CategoriaService categoriaService) {
        this.chamadoService = chamadoService;
        this.clienteService = clienteService;
        this.tecnicoService = tecnicoService;
        this.categoriaService = categoriaService;
    }

    @GetMapping({"/", "/ui"})
    public String painel(Model model) {
        Map<StatusChamado, Long> porStatus = new LinkedHashMap<>();
        long total = 0;
        for (StatusChamado s : StatusChamado.values()) {
            long qtd = chamadoService.listar(s, null, null, PageRequest.of(0, 1)).getTotalElements();
            porStatus.put(s, qtd);
            total += qtd;
        }
        model.addAttribute("porStatus", porStatus);
        model.addAttribute("totalChamados", total);
        model.addAttribute("totalClientes", clienteService.listar().size());
        model.addAttribute("totalTecnicos", tecnicoService.listar().size());
        model.addAttribute("totalCategorias", categoriaService.listar().size());
        model.addAttribute("ultimosChamados", chamadoService.listar(null, null, null,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dataAbertura"))).getContent());
        return "dashboard";
    }
}
