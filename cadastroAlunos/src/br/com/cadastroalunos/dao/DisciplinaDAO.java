package br.com.cadastroalunos.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.cadastroalunos.model.Disciplina;
import br.com.cadastroalunos.util.ConexaoDB;

/**
 * DisciplinaDAO - Data Access Object (DAO) da entidade Disciplina.
 *
 * Responsável pela comunicação com o banco de dados relacionada às disciplinas.
 * Por enquanto possui apenas um método de listagem por curso, mas segue o mesmo
 * padrão dos outros DAOs do projeto.
 *
 * ATENÇÃO: diferente do AlunoDAO e CursoDAO, essa classe NÃO usa
 * try-with-resources — a conexão, o PreparedStatement e o ResultSet são
 * fechados manualmente no final com rs.close() e stmt.close(). O ideal seria
 * refatorar para try-with-resources para evitar vazamento de conexão caso
 * ocorra algum erro no meio do caminho.
 */
public class DisciplinaDAO {

	/**
	 * Retorna todas as disciplinas vinculadas a um curso específico.
	 *
	 * Os resultados são ordenados primeiro pelo semestre e depois pelo nome, o que
	 * facilita a exibição em grade curricular ou telas de listagem.
	 *
	 * @param cursoId O ID do curso cujas disciplinas serão buscadas.
	 * @return Lista de objetos Disciplina. Retorna lista vazia se não houver
	 *         nenhuma.
	 * @throws SQLException Se ocorrer algum erro na consulta.
	 */
	public List<Disciplina> listarPorCurso(int cursoId) throws SQLException {

		List<Disciplina> lista = new ArrayList<>();

		// Filtramos pelo curso_id para trazer só as disciplinas daquele curso.
		// ORDER BY semestre, nome: ordena primeiro pelo semestre (1º, 2º, 3º...)
		// e dentro do mesmo semestre, em ordem alfabética pelo nome da disciplina.
		String sql = "SELECT * FROM disciplina " + "WHERE curso_id = ? " + "ORDER BY semestre, nome";

		// Abrimos a conexão e preparamos o statement com o SQL acima.
		// ATENÇÃO: aqui não está sendo usado try-with-resources.
		// Os recursos são fechados manualmente no final do método (rs.close() e
		// stmt.close()).
		// Se ocorrer uma exceção antes do close(), a conexão pode ficar aberta
		// (connection leak).
		PreparedStatement stmt = ConexaoDB.getConexao().prepareStatement(sql);

		// Define o valor do '?' no WHERE curso_id = ?
		stmt.setInt(1, cursoId);

		// Executa o SELECT e armazena o resultado no ResultSet.
		ResultSet rs = stmt.executeQuery();

		// Percorre cada linha do resultado e converte em objeto Disciplina.
		while (rs.next()) {

			// Usamos o construtor vazio + setters para preencher o objeto campo a campo.
			Disciplina d = new Disciplina();

			d.setId(rs.getInt("id"));
			d.setNome(rs.getString("nome"));
			d.setSemestre(rs.getInt("semestre"));
			d.setCursoId(rs.getInt("curso_id"));

			lista.add(d);
		}

		// Fechamento manual dos recursos — importante liberar a conexão com o banco.
		// O ideal seria usar try-with-resources para garantir o fechamento mesmo em
		// caso de erro.
		rs.close();
		stmt.close();

		return lista;
	}
}