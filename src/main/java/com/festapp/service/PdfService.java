package com.festapp.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    // Fix: sanitiza inputs para evitar injeção de conteúdo no PDF
    private String safe(String s, int maxLen) {
        if (s == null) return "";
        s = s.replace("<", "").replace(">", "").replace("&", "").trim();
        return s.length() > maxLen ? s.substring(0, maxLen) : s;
    }

    public byte[] gerarGuiaBoasVindas(String nomeEmpresa, String plano) throws Exception {

        // Fix: limita tamanho e sanitiza inputs antes de usar no PDF
        nomeEmpresa = safe(nomeEmpresa, 100);
        plano       = safe(plano, 20);

        Document document = new Document(PageSize.A4, 50, 50, 60, 60);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        BaseColor azulEscuro = new BaseColor(30, 58, 138);
        BaseColor azulClaro  = new BaseColor(219, 234, 254);
        BaseColor cinza      = new BaseColor(107, 114, 128);
        BaseColor verde      = new BaseColor(22, 163, 74);

        Font fonteSubtitulo = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, azulEscuro);
        Font fonteNormal    = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
        Font fonteCinza     = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, cinza);
        Font fontePasso     = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, azulEscuro);
        Font fonteVerde     = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, verde);

        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(azulEscuro);
        headerCell.setPadding(20);
        headerCell.setBorder(Rectangle.NO_BORDER);

        Font fonteTituloBlanco = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.WHITE);
        Paragraph tituloBlanco = new Paragraph("FestApp", fonteTituloBlanco);
        tituloBlanco.setAlignment(Element.ALIGN_CENTER);

        Font fonteSubBlanco = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.WHITE);
        Paragraph subBlanco = new Paragraph("Guia de Boas-Vindas", fonteSubBlanco);
        subBlanco.setAlignment(Element.ALIGN_CENTER);

        headerCell.addElement(tituloBlanco);
        headerCell.addElement(subBlanco);
        header.addCell(headerCell);
        document.add(header);
        document.add(Chunk.NEWLINE);

        String nomePlano = switch (plano.toUpperCase()) {
            case "PROFISSIONAL" -> "Profissional";
            case "PREMIUM"      -> "Premium";
            default             -> "Básico";
        };

        Paragraph boasVindas = new Paragraph("Bem-vinda ao FestApp, " + nomeEmpresa + "!", fonteVerde);
        boasVindas.setAlignment(Element.ALIGN_CENTER);
        document.add(boasVindas);
        document.add(Chunk.NEWLINE);

        Paragraph planoAtual = new Paragraph("Plano contratado: " + nomePlano, fonteSubtitulo);
        planoAtual.setAlignment(Element.ALIGN_CENTER);
        document.add(planoAtual);
        document.add(Chunk.NEWLINE);

        Paragraph intro = new Paragraph(
                "Obrigado por escolher o FestApp! Este guia vai te ajudar a comecar a usar "
                        + "o sistema de forma rapida e eficiente. Em poucos minutos voce ja estara "
                        + "gerenciando sua empresa de festas como uma profissional.",
                fonteNormal
        );
        intro.setAlignment(Element.ALIGN_JUSTIFIED);
        document.add(intro);
        document.add(Chunk.NEWLINE);

        LineSeparator linha = new LineSeparator();
        linha.setLineColor(azulClaro);
        document.add(new Chunk(linha));
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph("Como comecar", fonteSubtitulo));
        document.add(Chunk.NEWLINE);

        String[][] passos = {
                {"1. Cadastre seus funcionarios",
                        "Acesse a aba Funcionarios e cadastre sua equipe com nome, email e telefone."},
                {"2. Cadastre suas festas",
                        "Na aba Festas, registre os eventos com data, horario, endereco e quantidade de funcionarios."},
                {"3. Acompanhe o ponto",
                        "Na aba Ponto, seus funcionarios registram a chegada. Voce recebe alertas automaticos por email."},
                {"4. Gerencie o financeiro",
                        "Na aba Financeiro, registre valores cobrados e pagos por festa."},
                {"5. Acompanhe o Dashboard",
                        "O Dashboard mostra receita do mes, festas do dia e da semana."},
                {"6. Relatorio de funcionarios",
                        "Na aba Relatorio, acompanhe pontualidade, avaliacao e historico de cada funcionario."}
        };

        for (String[] passo : passos) {
            PdfPTable tabela = new PdfPTable(1);
            tabela.setWidthPercentage(100);
            tabela.setSpacingBefore(8);

            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(azulClaro);
            cell.setPadding(12);
            cell.setBorderColor(azulEscuro);
            cell.setBorderWidth(0.5f);
            cell.addElement(new Paragraph(passo[0], fontePasso));
            cell.addElement(new Paragraph(passo[1], fonteNormal));

            tabela.addCell(cell);
            document.add(tabela);
        }

        document.add(Chunk.NEWLINE);
        document.add(new Chunk(linha));
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph("Suporte", fonteSubtitulo));
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("Nossa equipe esta disponivel para te ajudar com qualquer duvida.", fonteNormal));
        document.add(Chunk.NEWLINE);

        PdfPTable tabelaSuporte = new PdfPTable(2);
        tabelaSuporte.setWidthPercentage(100);
        tabelaSuporte.setWidths(new float[]{1, 3});

        String[][] contatos = {
                {"Email",    "suporte@festapp.com.br"},
                {"WhatsApp", "(83) 9 9999-9999"},
                {"Horario",  "Segunda a Sexta, 8h as 18h"}
        };

        for (String[] contato : contatos) {
            PdfPCell label = new PdfPCell(new Phrase(contato[0], fontePasso));
            label.setPadding(8);
            label.setBorderColor(azulClaro);

            PdfPCell valor = new PdfPCell(new Phrase(contato[1], fonteNormal));
            valor.setPadding(8);
            valor.setBorderColor(azulClaro);

            tabelaSuporte.addCell(label);
            tabelaSuporte.addCell(valor);
        }

        document.add(tabelaSuporte);
        document.add(Chunk.NEWLINE);

        Paragraph rodape = new Paragraph(
                "FestApp - Sistema de gestao para empresas de festas e eventos\n"
                        + "Este documento foi gerado automaticamente apos a confirmacao do pagamento.",
                fonteCinza
        );
        rodape.setAlignment(Element.ALIGN_CENTER);
        document.add(rodape);

        document.close();
        return baos.toByteArray();
    }
}