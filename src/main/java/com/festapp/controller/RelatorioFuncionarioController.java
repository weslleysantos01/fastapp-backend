package com.festapp.controller;

import com.festapp.model.Funcionario;
import com.festapp.model.enums.PlanoTipo;
import com.festapp.repository.EmpresaRepository;
import com.festapp.repository.EscalaRepository;
import com.festapp.repository.FuncionarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/relatorio/funcionarios")
@CrossOrigin(origins = "http://localhost:3000")
public class RelatorioFuncionarioController {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private EscalaRepository escalaRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    private boolean temAcessoRelatorio(Long empresaId) {
        return empresaRepository.findById(empresaId)
                .map(e -> e.getPlano() == PlanoTipo.PROFISSIONAL || e.getPlano() == PlanoTipo.PREMIUM)
                .orElse(false);
    }

    @GetMapping
    public ResponseEntity<?> getRelatorio(HttpServletRequest request) {
        Long empresaId = (Long) request.getAttribute("empresa_id");

        // Bloqueia no backend — não confia apenas no frontend
        if (!temAcessoRelatorio(empresaId)) {
            return ResponseEntity.status(403)
                    .body(Map.of("erro", "Plano não inclui acesso a relatórios"));
        }

        List<Funcionario> funcionarios = funcionarioRepository.findByEmpresaId(empresaId);
        List<Object[]> estatisticas = escalaRepository.estatisticasPorFuncionario(empresaId);

        Map<Long, Object[]> statsMap = new HashMap<>();
        for (Object[] row : estatisticas) {
            statsMap.put(((Number) row[0]).longValue(), row);
        }

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Funcionario f : funcionarios) {
            Object[] stats = statsMap.get(f.getId());
            long totalFestas = stats != null ? ((Number) stats[1]).longValue() : 0;
            long noPrazo    = stats != null ? ((Number) stats[2]).longValue() : 0;
            long atrasados  = stats != null ? ((Number) stats[3]).longValue() : 0;
            // Fix: pontualidade calculada como long para evitar ClassCastException no sort
            long pontualidade = totalFestas > 0 ? Math.round(noPrazo * 100.0 / totalFestas) : 100L;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", f.getId());
            item.put("nome", f.getNome());
            item.put("email", f.getEmail());
            item.put("avaliacaoMedia", f.getAvaliacaoMedia());
            item.put("totalFestas", totalFestas);
            item.put("noPrazo", noPrazo);
            item.put("atrasados", atrasados);
            item.put("pontualidade", pontualidade);
            item.put("statusDia", f.getStatusDia());
            item.put("ativo", f.getAtivo());
            resultado.add(item);
        }

        resultado.sort(Comparator
                .comparingLong((Map<String, Object> m) -> (Long) m.get("pontualidade"))
                .reversed()
                .thenComparingLong(m -> (Long) m.get("totalFestas"))
        );

        return ResponseEntity.ok(resultado);
    }
}