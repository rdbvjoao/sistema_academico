package br.com.cadastroalunos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.cadastroalunos.model.Curso;
import br.com.cadastroalunos.util.ConexaoDB;

/**
 * CursoDAO - Data Access Object (DAO) da entidade Curso.
 *
 * Assim como o AlunoDAO, essa classe centraliza toda a comunicação com o banco
 * de dados relacionada aos cursos. Qualquer SQL que envolva a tabela "curso"
 * fica aqui, separado do restante do sistema.
 *
 * Operações disponíveis: salvar, alterar, excluir, buscar e listar cursos, além
 * de consultas específicas como nomes distintos e cursos únicos.
 */
public class CursoDAO {

	/**
	 * Salva um novo curso no banco de dados.
	 *
	 * Após inserir, o ID gerado automaticamente pelo banco (auto_increment) é
	 * recuperado e atribuído de volta ao objeto curso.
	 *
	 * @param curso Objeto com os dados do curso a ser cadastrado.
	 * @throws SQLException Se ocorrer algum erro durante a inserção.
	 */
	public void salvar(Curso curso) throws SQLException {

		// SQL de inserção. Os '?' serão substituídos pelos dados do objeto curso.
		String sqlInsercao = "INSERT INTO curso(nome, campus, periodo) VALUES (?, ?, ?)";

		// RETURN_GENERATED_KEYS diz pro banco que queremos recuperar o ID gerado.
		try (Connection conexao = ConexaoDB.getConexao();
				PreparedStatement preparedStatement = conexao.prepareStatement(sqlInsercao,
						Statement.RETURN_GENERATED_KEYS)) {

			// Preenchemos os '?' na ordem em que aparecem no SQL (começa em 1).
			preparedStatement.setString(1, curso.getNome());
			preparedStatement.setString(2, curso.getCampus());
			preparedStatement.setString(3, curso.getPeriodo());

			preparedStatement.executeUpdate();

			// Recupera o ID que o banco gerou automaticamente e seta no objeto.
			ResultSet resultadoGerado = preparedStatement.getGeneratedKeys();

			if (resultadoGerado.next()) {
				curso.setId(resultadoGerado.getInt(1));
			}
		}
	}

	/**
	 * Atualiza os dados de um curso já existente no banco.
	 *
	 * Identifica qual curso atualizar pelo ID (que nunca muda).
	 *
	 * @param curso Objeto com os dados atualizados do curso.
	 * @throws SQLException Se ocorrer algum erro durante a atualização.
	 */
	public void alterar(Curso curso) throws SQLException {

		// O WHERE id=? garante que só o curso com esse ID seja alterado.
		String sqlAtualizacao = "UPDATE curso SET nome=?, campus=?, periodo=? WHERE id=?";

		try (Connection conexao = ConexaoDB.getConexao();
				PreparedStatement preparedStatement = conexao.prepareStatement(sqlAtualizacao)) {

			preparedStatement.setString(1, curso.getNome());
			preparedStatement.setString(2, curso.getCampus());
			preparedStatement.setString(3, curso.getPeriodo());
			// O ID vem por último porque é o critério do WHERE.
			preparedStatement.setInt(4, curso.getId());

			preparedStatement.executeUpdate();
		}
	}

	/**
	 * Remove um curso do banco de dados pelo ID informado.
	 *
	 * Se o ID não existir, nenhuma linha é afetada e nenhum erro é lançado.
	 *
	 * @param id O ID do curso a ser excluído.
	 * @throws SQLException Se ocorrer algum erro durante a exclusão.
	 */
	public void excluir(int id) throws SQLException {

		String sqlExclusao = "DELETE FROM curso WHERE id=?";

		try (Connection conexao = ConexaoDB.getConexao();
				PreparedStatement preparedStatement = conexao.prepareStatement(sqlExclusao)) {

			preparedStatement.setInt(1, id);
			preparedStatement.executeUpdate();
		}
	}

	/**
	 * Retorna uma lista com todos os cursos cadastrados no banco.
	 *
	 * Os resultados são ordenados por nome, campus e período, facilitando a
	 * exibição em telas de listagem.
	 *
	 * @return Lista de objetos Curso. Retorna lista vazia se não houver nenhum
	 *         cadastrado.
	 * @throws SQLException Se ocorrer algum erro na consulta.
	 */
	public List<Curso> listarTodos() throws SQLException {

		List<Curso> listaCursos = new ArrayList<>();

		// ORDER BY nome, campus, periodo: ordena em três níveis — primeiro por nome,
		// depois por campus (dentro do mesmo nome), depois por período.
		String sqlConsulta = "SELECT * FROM curso ORDER BY nome, campus, periodo";

		// Usamos Statement simples porque não há filtros com parâmetros nessa consulta.
		try (Connection conexao = ConexaoDB.getConexao();
				Statement statement = conexao.createStatement();
				ResultSet resultadoConsulta = statement.executeQuery(sqlConsulta)) {

			while (resultadoConsulta.next()) {

				// Criamos o objeto Curso já passando os dados pelo construtor (forma mais
				// direta).
				Curso curso = new Curso(resultadoConsulta.getInt("id"), resultadoConsulta.getString("nome"),
						resultadoConsulta.getString("campus"), resultadoConsulta.getString("periodo"));

				listaCursos.add(curso);
			}
		}

		return listaCursos;
	}

	/**
	 * Busca um curso específico pelo ID.
	 *
	 * @param id O ID do curso a ser buscado.
	 * @return O objeto Curso encontrado, ou null se não existir.
	 * @throws SQLException Se ocorrer algum erro na consulta.
	 */
	public Curso buscarPorId(int id) throws SQLException {

		String sqlConsulta = "SELECT * FROM curso WHERE id=?";

		try (Connection conexao = ConexaoDB.getConexao();
				PreparedStatement preparedStatement = conexao.prepareStatement(sqlConsulta)) {

			preparedStatement.setInt(1, id);

			ResultSet resultadoConsulta = preparedStatement.executeQuery();

			// Se encontrou o curso, cria e retorna o objeto direto.
			// Se não encontrou, o if não entra e retorna null lá embaixo.
			if (resultadoConsulta.next()) {

				return new Curso(resultadoConsulta.getInt("id"), resultadoConsulta.getString("nome"),
						resultadoConsulta.getString("campus"), resultadoConsulta.getString("periodo"));
			}
		}

		// Retorna null se nenhum curso com esse ID foi encontrado.
		return null;
	}

	/**
	 * Retorna uma lista com os nomes únicos de cursos (sem repetição).
	 *
	 * Útil para preencher filtros ou comboboxes onde só queremos mostrar cada nome
	 * de curso uma vez, independente do campus ou período.
	 *
	 * @return Lista de Strings com os nomes distintos, em ordem alfabética.
	 * @throws SQLException Se ocorrer algum erro na consulta.
	 */
	public List<String> listarNomesDistintos() throws SQLException {

		List<String> listaNomesCursos = new ArrayList<>();

		// DISTINCT elimina nomes repetidos — se "Sistemas de Informação" existe
		// em dois campus, aparece só uma vez na lista.
		String sqlConsulta = "SELECT DISTINCT nome FROM curso ORDER BY nome";

		try (Connection conexao = ConexaoDB.getConexao();
				Statement statement = conexao.createStatement();
				ResultSet resultadoConsulta = statement.executeQuery(sqlConsulta)) {

			while (resultadoConsulta.next()) {
				// Aqui só precisamos da String do nome, não de um objeto Curso inteiro.
				listaNomesCursos.add(resultadoConsulta.getString("nome"));
			}
		}

		return listaNomesCursos;
	}

	/**
	 * Retorna uma lista com os campus únicos cadastrados (sem repetição).
	 *
	 * Mesma ideia do listarNomesDistintos(), mas para a coluna campus. Útil para
	 * preencher filtros de campus em telas de busca.
	 *
	 * @return Lista de Strings com os campus distintos, em ordem alfabética.
	 * @throws SQLException Se ocorrer algum erro na consulta.
	 */
	public List<String> listarCampusDistintos() throws SQLException {

		List<String> listaCampus = new ArrayList<>();

		// DISTINCT garante que cada campus apareça só uma vez, mesmo que
		// exista mais de um curso naquele campus.
		String sqlConsulta = "SELECT DISTINCT campus FROM curso ORDER BY campus";

		try (Connection conexao = ConexaoDB.getConexao();
				Statement statement = conexao.createStatement();
				ResultSet resultadoConsulta = statement.executeQuery(sqlConsulta)) {

			while (resultadoConsulta.next()) {
				listaCampus.add(resultadoConsulta.getString("campus"));
			}
		}

		return listaCampus;
	}

	/**
	 * Retorna uma lista de cursos agrupados por nome (um representante por nome).
	 *
	 * Diferente do listarTodos(), que traz todas as combinações de
	 * nome/campus/período, esse método agrupa pelo nome e retorna apenas um
	 * registro por curso. O MIN(id) garante que pegamos o menor ID dentre os
	 * registros daquele grupo.
	 *
	 * Útil para situações onde precisamos de uma lista sem duplicidade de nomes,
	 * mas ainda precisamos do objeto Curso completo (e não só do nome).
	 *
	 * @return Lista de objetos Curso com nomes únicos.
	 * @throws SQLException Se ocorrer algum erro na consulta.
	 */
	public List<Curso> listarCursosUnicos() throws SQLException {

		List<Curso> lista = new ArrayList<>();

		// GROUP BY nome: agrupa todos os registros com o mesmo nome em um só.
		// MIN(id): como pode haver vários IDs no grupo, pegamos o menor deles
		// como representante — só precisamos de um ID válido.
		String sql = "SELECT MIN(id) AS id, nome, campus, periodo " + "FROM curso " + "GROUP BY nome";

		// PreparedStatement sem parâmetros aqui — poderíamos usar Statement simples,
		// mas PreparedStatement também funciona normalmente para consultas fixas.
		try (Connection conexao = ConexaoDB.getConexao();
				PreparedStatement ps = conexao.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {

				// Aqui usamos o construtor vazio + setters (diferente do listarTodos
				// que usa o construtor com parâmetros — as duas formas são válidas).
				Curso curso = new Curso();

				curso.setId(rs.getInt("id"));
				curso.setNome(rs.getString("nome"));
				curso.setCampus(rs.getString("campus"));
				curso.setPeriodo(rs.getString("periodo"));

				lista.add(curso);
			}
		}

		return lista;
	}

	/**
	 * Verifica se já existe um curso cadastrado com o nome informado.
	 *
	 * Usada para evitar cadastrar dois cursos com o mesmo nome. A comparação é
	 * feita direto no banco com COUNT(*).
	 *
	 * @param nome O nome do curso a ser verificado.
	 * @return true se já existir um curso com esse nome, false caso contrário.
	 * @throws SQLException Se ocorrer algum erro na consulta.
	 */
	public boolean cursoJaExiste(String nome) throws SQLException {

		// COUNT(*) conta quantos registros têm esse nome.
		// Se for maior que 0, o curso já existe.
		String sql = "SELECT COUNT(*) FROM curso WHERE nome = ?";

		try (Connection conexao = ConexaoDB.getConexao(); PreparedStatement ps = conexao.prepareStatement(sql)) {

			ps.setString(1, nome);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				// getInt(1) pega o valor da primeira coluna do resultado (o COUNT).
				return rs.getInt(1) > 0;
			}
		}

		return false;
	}
}