package com.festapp.service;

import com.festapp.model.Financeiro;
import com.festapp.repository.FinanceiroRepository;
import com.festapp.repository.FestaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FinanceiroService {

    @Autowired
    private FinanceiroRepository financeiroRepository;

    @Autowired
    private FestaRepository festaRepository;

    public List<Financeiro> listarTodos(Long empresaId) {
        if (empresaId == null) {
            throw new RuntimeException("Não autorizado");
        }
        return financeiroRepository.findByEmpresaId(empresaId);
    }

    public Financeiro salvar(Long festaId, Double valorCobrado,
                             Double valorPagoEquipe, Long empresaId) {

        if (empresaId == null) {
            throw new RuntimeException("Não autorizado");
        }

        if (festaId == null || valorCobrado == null) {
            throw new RuntimeException("Dados inválidos");
        }

        var festa = festaRepository.findByIdAndEmpresaId(festaId, empresaId)
                .orElseThrow(() -> new RuntimeException("Festa não encontrada"));

        Financeiro fin = financeiroRepository
                .findByFestaIdAndEmpresaId(festaId, empresaId)
                .orElse(new Financeiro());

        fin.setEmpresaId(empresaId);
        fin.setFesta(festa);
        fin.setValorCobrado(valorCobrado);
        fin.setValorPagoEquipe(valorPagoEquipe != null ? valorPagoEquipe : 0.0);

        return financeiroRepository.save(fin);
    }

    public Financeiro atualizarStatus(Long id, String status, Long empresaId) {

        if (empresaId == null) {
            throw new RuntimeException("Não autorizado");
        }

        if (id == null) {
            throw new RuntimeException("ID inválido");
        }

        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status inválido");
        }

        // 🔒 validação de enum (defesa extra)
        if (!List.of("PENDENTE", "PAGO", "CANCELADO").contains(status)) {
            throw new RuntimeException("Status inválido");
        }

        Financeiro fin = financeiroRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Registro não encontrado"));

        fin.setStatusPagamento(status);
        return financeiroRepository.save(fin);
    }

    public Map<String, Double> resumo(Long empresaId) {

        if (empresaId == null) {
            throw new RuntimeException("Não autorizado");
        }

        Double receita = financeiroRepository.totalReceita(empresaId);
        Double custo = financeiroRepository.totalCusto(empresaId);

        receita = (receita != null) ? receita : 0.0;
        custo = (custo != null) ? custo : 0.0;

        return Map.of(
                "receita", receita,
                "custo", custo,
                "lucro", receita - custo
        );
    }
}