package br.com.cadastroalunos.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import br.com.cadastroalunos.model.Aluno;
import br.com.cadastroalunos.util.ConexaoDB;

/**
 * AlunoDAO - Data Access Object (DAO) da entidade Aluno.
 *
 * Essa classe é responsável por toda a comunicação com o banco de dados
 * relacionada aos alunos. O padrão DAO serve pra separar a lógica de acesso ao
 * banco do restante da aplicação — ou seja, qualquer coisa que envolva SQL de
 * aluno, passa por aqui.
 *
 * Operações disponíveis: salvar, alterar, excluir, buscar e listar alunos.
 */
public class AlunoDAO {

	/**
	 * Salva um novo aluno no banco de dados.
	 *
	 * Antes de inserir, verifica se já existe um aluno com o mesmo RGM para evitar
	 * duplicatas. Se existir, lança uma exceção.
	 *
	 * Após inserir com sucesso, o ID gerado automaticamente pelo banco
	 * (auto_increment) é recuperado e atribuído de volta ao objeto aluno.
	 *
	 * @param aluno Objeto com os dados do aluno a ser cadastrado.
	 * @throws SQLException Se o RGM já existir ou ocorrer erro no banco.
	 */
	public void salvar(Aluno aluno) throws SQLException {

		// Antes de qualquer coisa, checamos se o RGM já está cadastrado.
		// Se sim, não faz sentido continuar — lançamos a exceção aqui mesmo.
		if (existeRgm(aluno.getRgm())) {
			throw new SQLException("Já existe um aluno cadastrado com o RGM: " + aluno.getRgm());
		}

		// SQL de inserção. Usamos '?' como marcadores de posição para os valores.
		// O STR_TO_DATE converte a data que vem no formato brasileiro (dd/MM/yyyy)
		// para o formato que o MySQL entende internamente.
		String sqlInsercao = "INSERT INTO aluno (rgm, nome, data_nascimento, cpf, email, endereco, municipio, uf, celular, curso_id) "
				+ "VALUES (?, ?, STR_TO_DATE(?, '%d/%m/%Y'), ?, ?, ?, ?, ?, ?, ?)";

		// try-with-resources: garante que a conexão e o PreparedStatement
		// sejam fechados automaticamente ao fim do bloco, mesmo se der erro.
		// O RETURN_GENERATED_KEYS diz pro banco que queremos recuperar o ID gerado.
		try (Connection conexao = ConexaoDB.getConexao();
				PreparedStatement ps = conexao.prepareStatement(sqlInsercao, Statement.RETURN_GENERATED_KEYS)) {

			// Preenchemos cada '?' do SQL na ordem em que aparecem (começa em 1, não em 0).
			ps.setString(1, aluno.getRgm());
			ps.setString(2, aluno.getNome());
			ps.setString(3, aluno.getDataNascimento()); // ex: "15/03/2001" — o STR_TO_DATE cuida da conversão
			ps.setString(4, aluno.getCpf());
			ps.setString(5, aluno.getEmail());
			ps.setString(6, aluno.getEndereco());
			ps.setString(7, aluno.getMunicipio());
			ps.setString(8, aluno.getUf());
			ps.setString(9, aluno.getCelular());

			// O curso é opcional: se o aluno não tiver curso vinculado (cursoId <= 0),
			// salvamos NULL no banco em vez de um número inválido.
			if (aluno.getCursoId() > 0) {
				ps.setInt(10, aluno.getCursoId());
			} else {
				ps.setNull(10, Types.INTEGER);
			}

			// Executa o INSERT no banco.
			ps.executeUpdate();

			// Recupera o ID gerado automaticamente pelo banco (auto_increment)
			// e seta no objeto aluno, pra quem chamou esse método ter o ID atualizado.
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					aluno.setId(rs.getInt(1));
				}
			}
		}
	}

	/**
	 * Atualiza os dados de um aluno já existente no banco.
	 *
	 * Identifica qual aluno atualizar pelo RGM (que não muda). Os demais campos são
	 * sobrescritos com os novos valores.
	 *
	 * @param aluno Objeto com os dados atualizados do aluno.
	 * @throws SQLException Se ocorrer algum erro durante a atualização.
	 */
	public void alterar(Aluno aluno) throws SQLException {

		// SQL de atualização. O WHERE garante que só o aluno com esse RGM seja
		// alterado.
		// Assim como no salvar(), a data passa pelo STR_TO_DATE para conversão de
		// formato.
		String sqlAtualizacao = "UPDATE aluno SET nome=?, data_nascimento=STR_TO_DATE(?, '%d/%m/%Y'), cpf=?, "
				+ "email=?, endereco=?, municipio=?, uf=?, celular=?, curso_id=? WHERE rgm=?";

		try (Connection conexao = ConexaoDB.getConexao();
				PreparedStatement ps = conexao.prepareStatement(sqlAtualizacao)) {

			// Preenchemos os '?' na mesma ordem que aparecem no SQL.
			ps.setString(1, aluno.getNome());
			ps.setString(2, aluno.getDataNascimento());
			ps.setString(3, aluno.getCpf());
			ps.setString(4, aluno.getEmail());
			ps.setString(5, aluno.getEndereco());
			ps.setString(6, aluno.getMunicipio());
			ps.setString(7, aluno.getUf());
			ps.setString(8, aluno.getCelular());

			// Mesma lógica do salvar: curso é opcional, pode ser NULL.
			if (aluno.getCursoId() > 0) {
				ps.setInt(9, aluno.getCursoId());
			} else {
				ps.setNull(9, Types.INTEGER);
			}

			// O RGM vem por último porque é o critério do WHERE.
			ps.setString(10, aluno.getRgm());

			ps.executeUpdate();
		}
	}

	/**
	 * Remove um aluno do banco de dados com base no RGM informado.
	 *
	 * Se o RGM não for encontrado, nenhuma linha é afetada e nenhum erro é lançado.
	 *
	 * @param rgm O RGM do aluno a ser excluído.
	 * @throws SQLException Se ocorrer algum erro durante a exclusão.
	 */
	public void excluir(String rgm) throws SQLException {

		String sqlExclusao = "DELETE FROM aluno WHERE rgm = ?";

		try (Connection conexao = ConexaoDB.getConexao();
				PreparedStatement ps = conexao.prepareStatement(sqlExclusao)) {

			ps.setString(1, rgm);
			ps.executeUpdate();
		}
	}

	/**
	 * Busca um aluno específico pelo número do RGM.
	 *
	 * Faz um JOIN com a tabela de cursos para trazer também os dados do curso em
	 * que o aluno está matriculado (se houver). O LEFT JOIN garante que alunos sem
	 * curso também sejam retornados.
	 *
	 * @param rgm O RGM do aluno que deseja buscar.
	 * @return O objeto Aluno encontrado, ou null se não existir.
	 * @throws SQLException Se ocorrer algum erro na consulta.
	 */
	public Aluno buscarPorRgm(String rgm) throws SQLException {

		// LEFT JOIN: mesmo que o aluno não tenha curso, ele aparece no resultado.
		// Se usássemos INNER JOIN, alunos sem curso seriam ignorados.
		String sqlConsulta = "SELECT a.*, c.id AS curso_id, c.nome AS nome_curso, c.campus, c.periodo "
				+ "FROM aluno a LEFT JOIN curso c ON a.curso_id = c.id " + "WHERE a.rgm = ?";

		try (Connection conexao = ConexaoDB.getConexao();
				PreparedStatement ps = conexao.prepareStatement(sqlConsulta)) {

			ps.setString(1, rgm);

			try (ResultSet rs = ps.executeQuery()) {
				// Se encontrou resultado, converte e retorna o aluno.
				// Se não encontrou, cai fora do if e retorna null lá embaixo.
				if (rs.next()) {
					return mapearAluno(rs);
				}
			}
		}

		// Retorna null se nenhum aluno com esse RGM foi encontrado.
		return null;
	}

	/**
	 * Retorna uma lista com todos os alunos cadastrados no banco.
	 *
	 * Inclui dados do curso de cada aluno (quando houver) via LEFT JOIN. Os alunos
	 * são ordenados alfabeticamente pelo nome.
	 *
	 * @return Lista de objetos Aluno. Retorna lista vazia se não houver nenhum
	 *         cadastrado.
	 * @throws SQLException Se ocorrer algum erro na consulta.
	 */
	public List<Aluno> listarTodos() throws SQLException {

		// Criamos a lista que vai receber todos os alunos encontrados.
		List<Aluno> listaAlunos = new ArrayList<>();

		// ORDER BY a.nome: garante que a lista já venha em ordem alfabética.
		String sqlConsulta = "SELECT a.*, c.nome AS nome_curso, c.campus, c.periodo "
				+ "FROM aluno a LEFT JOIN curso c ON a.curso_id = c.id " + "ORDER BY a.nome";

		// Aqui usamos Statement simples (sem parâmetros), pois não filtramos nada.
		try (Connection conexao = ConexaoDB.getConexao();
				Statement st = conexao.createStatement();
				ResultSet rs = st.executeQuery(sqlConsulta)) {

			// Percorremos cada linha do resultado e convertemos em objeto Aluno.
			while (rs.next()) {
				listaAlunos.add(mapearAluno(rs));
			}
		}

		return listaAlunos;
	}

	/**
	 * Verifica se já existe um aluno cadastrado com o RGM informado.
	 *
	 * Usada principalmente pelo método salvar() para evitar duplicatas.
	 *
	 * @param rgm O RGM a ser verificado.
	 * @return true se já existir um aluno com esse RGM, false caso contrário.
	 * @throws SQLException Se ocorrer algum erro na consulta.
	 */
	public boolean existeRgm(String rgm) throws SQLException {

		// COUNT(*) conta quantos registros existem com esse RGM.
		// Se for maior que 0, significa que já existe.
		String sqlConsulta = "SELECT COUNT(*) AS total FROM aluno WHERE rgm = ?";

		try (Connection conexao = ConexaoDB.getConexao();
				PreparedStatement ps = conexao.prepareStatement(sqlConsulta)) {

			ps.setString(1, rgm);

			try (ResultSet rs = ps.executeQuery()) {
				// rs.next() avança para a primeira (e única) linha do resultado.
				// Se total > 0, retorna true (já existe).
				return rs.next() && rs.getInt("total") > 0;
			}
		}
	}

	/**
	 * Verifica se já existe um aluno cadastrado com o CPF informado.
	 *
	 * Útil para validar antes de salvar um novo aluno e garantir que não haverá
	 * dois registros com o mesmo CPF no banco.
	 *
	 * @param cpf O CPF a ser verificado.
	 * @return true se o CPF já estiver cadastrado, false caso contrário.
	 * @throws SQLException Se ocorrer algum erro na consulta.
	 */
	public boolean cpfJaExiste(String cpf) throws SQLException {

		// Mesma lógica do existeRgm(), mas verificando o CPF.
		String sql = "SELECT COUNT(*) FROM aluno WHERE cpf = ?";

		try (Connection conexao = ConexaoDB.getConexao(); PreparedStatement stmt = conexao.prepareStatement(sql)) {
			stmt.setString(1, cpf);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				// getInt(1) pega o valor da primeira coluna do resultado (o COUNT).
				return rs.getInt(1) > 0;
			}
		}

		return false;
	}

	/**
	 * Converte uma linha do ResultSet em um objeto Aluno.
	 *
	 * Método auxiliar (privado) chamado sempre que precisamos transformar o
	 * resultado bruto de um SELECT em um objeto Java que o sistema consegue usar.
	 *
	 * Também trata conversões necessárias, como: - Data SQL -> String no formato
	 * brasileiro (dd/MM/yyyy) - curso_id NULL do banco -> valor 0 no objeto
	 *
	 * @param rs ResultSet posicionado na linha a ser lida.
	 * @return Objeto Aluno preenchido com os dados da linha.
	 * @throws SQLException Se ocorrer algum erro ao ler os dados do ResultSet.
	 */
	private Aluno mapearAluno(ResultSet rs) throws SQLException {

		Aluno aluno = new Aluno();

		// Campos simples: lemos direto do ResultSet pelo nome da coluna no banco.
		aluno.setId(rs.getInt("id"));
		aluno.setRgm(rs.getString("rgm"));
		aluno.setNome(rs.getString("nome"));

		// A data vem do banco como tipo Date (SQL). Precisamos converter para
		// LocalDate do Java e depois formatar como string no padrão brasileiro.
		Date dataNascimento = rs.getDate("data_nascimento");

		if (dataNascimento != null) {
			java.time.LocalDate dataLocal = dataNascimento.toLocalDate();

			// %02d garante que dia e mês sempre tenham 2 dígitos (ex: 05, não 5).
			// %04d garante 4 dígitos no ano.
			aluno.setDataNascimento(String.format("%02d/%02d/%04d", dataLocal.getDayOfMonth(),
					dataLocal.getMonthValue(), dataLocal.getYear()));
		}

		aluno.setCpf(rs.getString("cpf"));
		aluno.setEmail(rs.getString("email"));
		aluno.setEndereco(rs.getString("endereco"));
		aluno.setMunicipio(rs.getString("municipio"));
		aluno.setUf(rs.getString("uf"));
		aluno.setCelular(rs.getString("celular"));

		// curso_id pode ser NULL no banco (aluno sem curso).
		// ATENÇÃO: wasNull() deve ser chamado LOGO APÓS o getInt() correspondente.
		// Ele verifica se o último campo lido foi NULL — se chamar fora de ordem, dá
		// resultado errado.
		int cursoId = rs.getInt("curso_id");

		if (!rs.wasNull() && cursoId > 0) {
			aluno.setCursoId(cursoId);
		} else {
			// Se veio NULL do banco, usamos 0 como valor padrão ("sem curso").
			aluno.setCursoId(0);
		}

		// Dados do curso vêm do JOIN com a tabela curso.
		// Se o aluno não tiver curso, esses campos virão como null.
		aluno.setNomeCurso(rs.getString("nome_curso"));
		aluno.setCampus(rs.getString("campus"));
		aluno.setPeriodo(rs.getString("periodo"));

		return aluno;
	}
}