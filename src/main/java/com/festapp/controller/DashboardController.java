package com.festapp.controller;

import com.festapp.model.Festa;
import com.festapp.repository.FestaRepository;
import com.festapp.repository.FinanceiroRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    @Autowired
    private FinanceiroRepository financeiroRepository;

    @Autowired
    private FestaRepository festaRepository;

    @GetMapping
    public Map<String, Object> getDashboard(HttpServletRequest request) {
        Long empresaId = (Long) request.getAttribute("empresa_id");

        LocalDateTime agora = LocalDateTime.now();
        int mes = agora.getMonthValue();
        int ano = agora.getYear();

        // Fix: nunca retorna null para o frontend
        Double receitaMesRaw = financeiroRepository.totalReceitaMes(empresaId, mes, ano);
        double receitaMes = receitaMesRaw != null ? receitaMesRaw : 0.0;

        // Festas hoje
        LocalDateTime inicioDia = agora.toLocalDate().atStartOfDay();
        LocalDateTime fimDia = agora.toLocalDate().atTime(23, 59, 59);
        List<Festa> festasHoje = festaRepository.findFestasHoje(empresaId, inicioDia, fimDia);

        // Festas da semana
        LocalDateTime inicioSemana = agora.with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        LocalDateTime fimSemana = agora.with(java.time.DayOfWeek.SUNDAY).toLocalDate().atTime(23, 59, 59);
        List<Festa> festasSemana = festaRepository.findByEmpresaIdAndDataHoraBetween(empresaId, inicioSemana, fimSemana);

        // Fix performance: ordenação feita no banco, não em memória
        List<Festa> ultimasFestas = festaRepository.findTop5ByEmpresaIdOrderByDataHoraDesc(empresaId);

        // Receita por mês (gráfico)
        List<Object[]> receitaPorMesRaw = financeiroRepository.receitaPorMes(empresaId, ano);
        List<Map<String, Object>> grafico = new ArrayList<>();
        String[] meses = {"Jan","Fev","Mar","Abr","Mai","Jun","Jul","Ago","Set","Out","Nov","Dez"};
        for (Object[] row : receitaPorMesRaw) {
            int numMes = ((Number) row[0]).intValue();
            grafico.add(Map.of(
                    "mes", meses[numMes - 1],
                    "receita", row[1]
            ));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("receitaMes", receitaMes);
        response.put("festasHoje", festasHoje.size());
        response.put("festasSemana", festasSemana.size());
        response.put("ultimasFestas", ultimasFestas);
        response.put("graficoReceita", grafico);

        return response;
    }
}