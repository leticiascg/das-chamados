package br.com.sistemas.chamados.web;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import br.com.sistemas.chamados.exception.ChamadoFechadoException;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;

/**
 * Trata erros SO das telas (pacote web) e mostra erro.html.
 * O GlobalExceptionHandler da API continua devolvendo JSON em /clientes, /chamados etc.
 */
@ControllerAdvice(basePackages = "br.com.sistemas.chamados.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String naoEncontrado(RecursoNaoEncontradoException ex, Model model) {
        model.addAttribute("titulo", "Não encontrado");
        model.addAttribute("mensagem", ex.getMessage());
        return "erro";
    }

    @ExceptionHandler({RegraNegocioException.class, ChamadoFechadoException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public String conflito(RuntimeException ex, Model model) {
        model.addAttribute("titulo", "Operação não permitida");
        model.addAttribute("mensagem", ex.getMessage());
        return "erro";
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String parametroInvalido(Model model) {
        model.addAttribute("titulo", "Endereço inválido");
        model.addAttribute("mensagem", "O endereço acessado tem um valor que o sistema não reconhece.");
        return "erro";
    }
}
