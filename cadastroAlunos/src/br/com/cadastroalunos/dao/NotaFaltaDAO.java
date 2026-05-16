package br.com.cadastroalunos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.cadastroalunos.model.NotaFalta;
import br.com.cadastroalunos.util.ConexaoDB;

public class NotaFaltaDAO {

    /**
     * Salva um novo registro. Grava faltas_a1 e faltas_a2 separadamente.
     * A coluna legada "faltas" recebe o total (faltasA1 + faltasA2) para
     * manter compatibilidade com registros antigos.
     */
    public void salvar(NotaFalta notaFalta) throws SQLException {

        String sql = "INSERT INTO nota_falta "
                + "(aluno_id, disciplina_id, semestre, a1, a2, a3, media, faltas, faltas_a1, faltas_a2) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, notaFalta.getAlunoId());
            ps.setInt(2, notaFalta.getDisciplinaId());
            ps.setString(3, notaFalta.getSemestre());
            ps.setDouble(4, notaFalta.getA1());
            ps.setDouble(5, notaFalta.getA2());
            ps.setDouble(6, notaFalta.getA3());
            ps.setDouble(7, notaFalta.getMedia());
            ps.setInt(8, notaFalta.getFaltas());      // total = faltasA1 + faltasA2
            ps.setInt(9, notaFalta.getFaltasA1());
            ps.setInt(10, notaFalta.getFaltasA2());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                notaFalta.setId(rs.getInt(1));
            }
        }
    }

    /**
     * Atualiza todos os campos de um registro existente.
     * Mantém "faltas" sincronizado com faltasA1 + faltasA2.
     */
    public void alterar(NotaFalta notaFalta) throws SQLException {

        String sql = "UPDATE nota_falta SET "
                + "aluno_id=?, disciplina_id=?, semestre=?, a1=?, a2=?, a3=?, media=?, "
                + "faltas=?, faltas_a1=?, faltas_a2=? "
                + "WHERE id=?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notaFalta.getAlunoId());
            ps.setInt(2, notaFalta.getDisciplinaId());
            ps.setString(3, notaFalta.getSemestre());
            ps.setDouble(4, notaFalta.getA1());
            ps.setDouble(5, notaFalta.getA2());
            ps.setDouble(6, notaFalta.getA3());
            ps.setDouble(7, notaFalta.getMedia());
            ps.setInt(8, notaFalta.getFaltas());      // total = faltasA1 + faltasA2
            ps.setInt(9, notaFalta.getFaltasA1());
            ps.setInt(10, notaFalta.getFaltasA2());
            ps.setInt(11, notaFalta.getId());

            ps.executeUpdate();
        }
    }

    /**
     * Remove um registro pelo ID.
     */
    public void excluir(int id) throws SQLException {

        String sql = "DELETE FROM nota_falta WHERE id = ?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Lista todas as notas de um aluno com JOIN em aluno e disciplina.
     * Lê faltas_a1 e faltas_a2 separadamente do banco.
     */
    public List<NotaFalta> listarPorAluno(int alunoId) throws SQLException {

        List<NotaFalta> lista = new ArrayList<>();

        String sql = "SELECT nf.*, d.nome AS nome_disciplina, "
                + "a.nome AS nome_aluno, a.rgm AS rgm_aluno "
                + "FROM nota_falta nf "
                + "JOIN aluno a ON nf.aluno_id = a.id "
                + "JOIN disciplina d ON nf.disciplina_id = d.id "
                + "WHERE nf.aluno_id = ? "
                + "ORDER BY nf.semestre, d.nome";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, alunoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                NotaFalta nf = new NotaFalta();
                nf.setId(rs.getInt("id"));
                nf.setAlunoId(rs.getInt("aluno_id"));
                nf.setDisciplinaId(rs.getInt("disciplina_id"));
                nf.setDisciplina(rs.getString("nome_disciplina"));
                nf.setSemestre(rs.getString("semestre"));
                nf.setA1(rs.getDouble("a1"));
                nf.setA2(rs.getDouble("a2"));
                nf.setA3(rs.getDouble("a3"));
                nf.setMedia(rs.getDouble("media"));
                // ✅ Lê as faltas separadas — getFaltas() soma automaticamente
                nf.setFaltasA1(rs.getInt("faltas_a1"));
                nf.setFaltasA2(rs.getInt("faltas_a2"));
                nf.setNomeAluno(rs.getString("nome_aluno"));
                nf.setRgmAluno(rs.getString("rgm_aluno"));
                lista.add(nf);
            }
        }

        return lista;
    }

    /**
     * Verifica duplicata antes de inserir.
     */
    public boolean disciplinaJaLancada(int alunoId, int disciplinaId, String semestre) throws SQLException {

        String sql = "SELECT COUNT(*) FROM nota_falta "
                + "WHERE aluno_id = ? AND disciplina_id = ? AND semestre = ?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, alunoId);
            ps.setInt(2, disciplinaId);
            ps.setString(3, semestre);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    /**
     * Salva ou atualiza somente a nota A1 e suas faltas.
     */
    public void salvarApenasA1(int alunoId, int disciplinaId, String nomeDisciplina,
                                String semestre, double a1, int faltasA1) throws SQLException {

        String sqlSelect = "SELECT id FROM nota_falta "
                + "WHERE aluno_id = ? AND disciplina_id = ? AND semestre = ?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sqlSelect)) {

            ps.setInt(1, alunoId);
            ps.setInt(2, disciplinaId);
            ps.setString(3, semestre);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String sqlUpdate = "UPDATE nota_falta SET a1=?, faltas_a1=?, "
                        + "faltas = faltas_a2 + ?, "
                        + "media = (a2 + ?) / 2.0 WHERE id=?";

                try (Connection conn2 = ConexaoDB.getConexao();
                     PreparedStatement psUp = conn2.prepareStatement(sqlUpdate)) {

                    psUp.setDouble(1, a1);
                    psUp.setInt(2, faltasA1);
                    psUp.setInt(3, faltasA1);    // recalcula faltas total
                    psUp.setDouble(4, a1);
                    psUp.setInt(5, id);
                    psUp.executeUpdate();
                }
            } else {
                String sqlInsert = "INSERT INTO nota_falta "
                        + "(aluno_id, disciplina_id, semestre, a1, a2, a3, media, faltas, faltas_a1, faltas_a2) "
                        + "VALUES (?, ?, ?, ?, 0, 0, 0, ?, ?, 0)";

                try (Connection conn2 = ConexaoDB.getConexao();
                     PreparedStatement psIns = conn2.prepareStatement(sqlInsert)) {

                    psIns.setInt(1, alunoId);
                    psIns.setInt(2, disciplinaId);
                    psIns.setString(3, semestre);
                    psIns.setDouble(4, a1);
                    psIns.setInt(5, faltasA1);   // faltas total = só A1 por ora
                    psIns.setInt(6, faltasA1);   // faltas_a1
                    psIns.executeUpdate();
                }
            }
        }
    }

    /**
     * Atualiza somente a nota A2 e suas faltas. Recalcula faltas total e média.
     */
    public boolean atualizarA2(int alunoId, int disciplinaId, String semestre,
                                double a2, int faltasA2) throws SQLException {

        String sql = "UPDATE nota_falta SET a2=?, faltas_a2=?, "
                + "faltas = faltas_a1 + ?, "
                + "media = (a1 + ?) / 2.0 "
                + "WHERE aluno_id=? AND disciplina_id=? AND semestre=?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, a2);
            ps.setInt(2, faltasA2);
            ps.setInt(3, faltasA2);    // recalcula faltas total
            ps.setDouble(4, a2);
            ps.setInt(5, alunoId);
            ps.setInt(6, disciplinaId);
            ps.setString(7, semestre);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Busca um registro específico por aluno + disciplina + semestre.
     * Lê faltas_a1 e faltas_a2 separadamente.
     */
    public NotaFalta buscarPorAlunoDisciplinaSemestre(int alunoId, int disciplinaId,
                                                       String semestre) throws SQLException {

        String sql = "SELECT * FROM nota_falta "
                + "WHERE aluno_id = ? AND disciplina_id = ? AND semestre = ?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, alunoId);
            ps.setInt(2, disciplinaId);
            ps.setString(3, semestre);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                NotaFalta nf = new NotaFalta();
                nf.setId(rs.getInt("id"));
                nf.setAlunoId(rs.getInt("aluno_id"));
                nf.setDisciplinaId(rs.getInt("disciplina_id"));
                nf.setSemestre(rs.getString("semestre"));
                nf.setA1(rs.getDouble("a1"));
                nf.setA2(rs.getDouble("a2"));
                nf.setA3(rs.getDouble("a3"));
                nf.setMedia(rs.getDouble("media"));
                // ✅ Lê as faltas separadas — getFaltas() soma automaticamente
                nf.setFaltasA1(rs.getInt("faltas_a1"));
                nf.setFaltasA2(rs.getInt("faltas_a2"));
                return nf;
            }
        }
        return null;
    }
}