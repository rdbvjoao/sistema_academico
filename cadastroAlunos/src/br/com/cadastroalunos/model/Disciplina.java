package br.com.cadastroalunos.model;

/**
 * Classe modelo que representa uma Disciplina no sistema acadêmico.
 *
 * <p>
 * Uma disciplina sempre pertence a um curso específico, vinculada pelo atributo
 * {@code cursoId} (chave estrangeira). Dessa forma, cada curso pode ter seu
 * próprio conjunto de disciplinas por semestre.
 * </p>
 *
 * <p>
 * Exemplo de relacionamento:
 * </p>
 * 
 * <pre>
 *   Curso (id=1, nome="ADS")
 *     └── Disciplina (id=1, nome="Algoritmos",   semestre=1, cursoId=1)
 *     └── Disciplina (id=2, nome="Banco de Dados", semestre=2, cursoId=1)
 * </pre>
 *
 * @author Equipe ADS
 * @version 1.0
 */
public class Disciplina {

	// =========================================================
	// ATRIBUTOS — cada um armazena uma informação da disciplina
	// =========================================================

	/**
	 * Identificador único gerado automaticamente pelo banco de dados (chave
	 * primária).
	 */
	private int id;

	/** Nome da disciplina (ex: "Estrutura de Dados", "Engenharia de Software"). */
	private String nome;

	/** Semestre em que a disciplina é cursada dentro do curso (ex: 1, 2, 3...). */
	private int semestre;

	/**
	 * Chave estrangeira que referencia o curso ao qual esta disciplina pertence.
	 * Corresponde ao {@code id} da tabela Curso no banco de dados.
	 */
	private int cursoId;

	// =========================================================
	// GETTERS E SETTERS
	// Como os atributos são "private", a única forma de acessá-los
	// de fora da classe é através desses métodos públicos.
	// Getter = lê o valor | Setter = altera o valor
	// =========================================================

	/**
	 * Retorna o ID da disciplina gerado pelo banco de dados.
	 * 
	 * @return id da disciplina
	 */
	public int getId() {
		return id;
	}

	/**
	 * Define o ID da disciplina — normalmente chamado pelo DAO após consulta no
	 * banco.
	 * 
	 * @param id identificador gerado pelo banco
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Retorna o nome da disciplina.
	 * 
	 * @return nome da disciplina (ex: "Programação Orientada a Objetos")
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Define o nome da disciplina.
	 * 
	 * @param nome nome da disciplina
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}

	/**
	 * Retorna o semestre em que a disciplina é cursada.
	 * 
	 * @return número do semestre (ex: 1 para primeiro semestre)
	 */
	public int getSemestre() {
		return semestre;
	}

	/**
	 * Define o semestre da disciplina.
	 * 
	 * @param semestre número do semestre dentro do curso
	 */
	public void setSemestre(int semestre) {
		this.semestre = semestre;
	}

	/**
	 * Retorna o ID do curso ao qual esta disciplina pertence.
	 * 
	 * @return cursoId (chave estrangeira referenciando a tabela Curso)
	 */
	public int getCursoId() {
		return cursoId;
	}

	/**
	 * Define o ID do curso da disciplina.
	 * 
	 * @param cursoId ID do curso na tabela Curso
	 */
	public void setCursoId(int cursoId) {
		this.cursoId = cursoId;
	}

	// =========================================================
	// MÉTODOS SOBRESCRITOS
	// =========================================================

	/**
	 * Retorna apenas o nome da disciplina como representação textual.
	 *
	 * <p>
	 * Sobrescreve o {@code toString()} padrão da classe Object. Retornar só o nome
	 * (sem campus, período etc.) é uma escolha intencional: disciplinas geralmente
	 * aparecem em listas filtradas por curso e semestre, então informações extras
	 * seriam redundantes.
	 * </p>
	 *
	 * <p>
	 * Exemplo de retorno: {@code "Banco de Dados"}
	 * </p>
	 *
	 * @return nome da disciplina
	 */
	@Override
	public String toString() {
		return nome;
	}
}