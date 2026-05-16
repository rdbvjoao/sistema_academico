package br.com.cadastroalunos.model;

/**
 * Classe modelo que representa as Notas e Faltas de um aluno em uma disciplina.
 *
 * <p>
 * Cada objeto desta classe corresponde a um registro de desempenho acadêmico,
 * vinculando um aluno a uma disciplina em um semestre específico.
 * </p>
 *
 * <p>
 * O sistema de avaliação utiliza três notas (A1, A2, A3) para calcular a média,
 * onde A3 funciona como prova de recuperação. A situação final é determinada
 * pelo método {@link #getSituacao()}, seguindo as regras:
 * </p>
 * <ul>
 * <li>Faltas >= 20 → <b>Reprovado por Falta</b> (independente das notas)</li>
 * <li>Média >= 6.0 → <b>Aprovado</b></li>
 * <li>Média < 6.0 e A3 == 0 → <b>Em Recuperação</b> (ainda não fez A3)</li>
 * <li>Média < 6.0 e A3 > 0 → <b>Reprovado</b></li>
 * </ul>
 *
 * @author Equipe ADS
 * @version 1.1
 */
public class NotaFalta {

	// =========================================================
	// ATRIBUTOS — dados principais do registro de nota/falta
	// =========================================================

	/**
	 * Identificador único gerado automaticamente pelo banco de dados (chave
	 * primária).
	 */
	private int id;

	/**
	 * Chave estrangeira que referencia o aluno dono deste registro. Corresponde ao
	 * {@code id} da tabela Aluno no banco de dados.
	 */
	private int alunoId;

	/**
	 * Nome da disciplina armazenado diretamente neste registro (desnormalizado para
	 * consultas rápidas).
	 */
	private String disciplina;

	/**
	 * Chave estrangeira que referencia a disciplina deste registro. Corresponde ao
	 * {@code id} da tabela Disciplina no banco de dados.
	 */
	private int disciplinaId;

	/** Semestre letivo ao qual este registro pertence (ex: "2026-1"). */
	private String semestre;

	/**
	 * Notas das três avaliações do aluno na disciplina. A3 funciona como prova de
	 * recuperação — valor 0.0 indica que ainda não foi realizada. Os três atributos
	 * são declarados na mesma linha por serem do mesmo tipo e contexto.
	 */
	private double a1, a2, a3;

	/**
	 * Média final do aluno na disciplina. Calculada e persistida pelo sistema — não
	 * é recalculada em tempo real por esta classe.
	 */
	private double media;

	/**
	 * Faltas registradas durante o período da primeira avaliação (A1). Armazenadas
	 * separadamente de {@code faltasA2} para permitir controle e edição individual
	 * por período de avaliação.
	 */
	private int faltasA1;

	/**
	 * Faltas registradas durante o período da segunda avaliação (A2). Armazenadas
	 * separadamente de {@code faltasA1} para permitir controle e edição individual
	 * por período de avaliação.
	 */
	private int faltasA2;

	// =========================================================
	// ATRIBUTOS AUXILIARES — preenchidos via JOIN com a tabela Aluno
	// Estes campos NÃO existem na tabela "nota_falta" do banco,
	// mas são trazidos junto na consulta para exibição na interface.
	// =========================================================

	/** Nome completo do aluno, obtido via JOIN com a tabela Aluno. */
	private String nomeAluno;

	/** RGM do aluno, obtido via JOIN com a tabela Aluno. */
	private String rgmAluno;

	// =========================================================
	// CONSTRUTORES
	// =========================================================

	/**
	 * Construtor padrão (vazio).
	 *
	 * Permite criar um objeto NotaFalta sem dados iniciais, preenchendo os
	 * atributos depois via setters. Usado, por exemplo, ao cadastrar um novo
	 * registro pela tela.
	 */
	public NotaFalta() {
	}

	/**
	 * Construtor completo — monta um objeto NotaFalta com todos os dados de uma
	 * vez.
	 *
	 * <p>
	 * Normalmente chamado pelo DAO ao converter o resultado de um SELECT (com JOIN)
	 * em um objeto Java pronto para uso na interface.
	 * </p>
	 *
	 * <p>
	 * Note que {@code disciplinaId} não está entre os parâmetros — ele pode ser
	 * setado separadamente via {@link #setDisciplinaId(int)} quando necessário.
	 * </p>
	 *
	 * @param id         ID do registro no banco de dados
	 * @param alunoId    ID do aluno (chave estrangeira)
	 * @param disciplina Nome da disciplina
	 * @param semestre   Semestre letivo (ex: "2026-1")
	 * @param a1         Nota da primeira avaliação
	 * @param a2         Nota da segunda avaliação
	 * @param a3         Nota da terceira avaliação / recuperação (0.0 se não
	 *                   realizada)
	 * @param media      Média final calculada
	 * @param faltasA1   Faltas registradas no período da A1
	 * @param faltasA2   Faltas registradas no período da A2
	 * @param nomeAluno  Nome do aluno (vindo do JOIN)
	 * @param rgmAluno   RGM do aluno (vindo do JOIN)
	 */
	public NotaFalta(int id, int alunoId, String disciplina, String semestre, double a1, double a2, double a3,
			double media, int faltasA1, int faltasA2, String nomeAluno, String rgmAluno) {
		this.id = id;
		this.alunoId = alunoId;
		this.disciplina = disciplina;
		this.semestre = semestre;
		this.a1 = a1;
		this.a2 = a2;
		this.a3 = a3;
		this.media = media;
		this.faltasA1 = faltasA1;
		this.faltasA2 = faltasA2;
		this.nomeAluno = nomeAluno;
		this.rgmAluno = rgmAluno;
	}

	// =========================================================
	// GETTERS E SETTERS
	// Como os atributos são "private", a única forma de acessá-los
	// de fora da classe é através desses métodos públicos.
	// Getter = lê o valor | Setter = altera o valor
	// =========================================================

	/**
	 * Retorna o ID do registro gerado pelo banco de dados.
	 * 
	 * @return id do registro
	 */
	public int getId() {
		return id;
	}

	/**
	 * Define o ID do registro — normalmente chamado pelo DAO após consulta no
	 * banco.
	 * 
	 * @param id identificador gerado pelo banco
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Retorna o ID do aluno vinculado a este registro.
	 * 
	 * @return alunoId (chave estrangeira referenciando a tabela Aluno)
	 */
	public int getAlunoId() {
		return alunoId;
	}

	/**
	 * Define o ID do aluno deste registro.
	 * 
	 * @param alunoId ID do aluno na tabela Aluno
	 */
	public void setAlunoId(int alunoId) {
		this.alunoId = alunoId;
	}

	/**
	 * Retorna o nome da disciplina armazenado neste registro.
	 * 
	 * @return nome da disciplina
	 */
	public String getDisciplina() {
		return disciplina;
	}

	/**
	 * Define o nome da disciplina deste registro.
	 * 
	 * @param disciplina nome da disciplina
	 */
	public void setDisciplina(String disciplina) {
		this.disciplina = disciplina;
	}

	/**
	 * Retorna o semestre letivo deste registro.
	 * 
	 * @return semestre (ex: "2026-1")
	 */
	public String getSemestre() {
		return semestre;
	}

	/**
	 * Define o semestre letivo deste registro.
	 * 
	 * @param semestre identificador do semestre (ex: "2026-1")
	 */
	public void setSemestre(String semestre) {
		this.semestre = semestre;
	}

	/**
	 * Retorna a nota da primeira avaliação (A1).
	 * 
	 * @return nota A1 (0.0 a 10.0)
	 */
	public double getA1() {
		return a1;
	}

	/**
	 * Define a nota da primeira avaliação.
	 * 
	 * @param a1 nota da A1
	 */
	public void setA1(double a1) {
		this.a1 = a1;
	}

	/**
	 * Retorna a nota da segunda avaliação (A2).
	 * 
	 * @return nota A2 (0.0 a 10.0)
	 */
	public double getA2() {
		return a2;
	}

	/**
	 * Define a nota da segunda avaliação.
	 * 
	 * @param a2 nota da A2
	 */
	public void setA2(double a2) {
		this.a2 = a2;
	}

	/**
	 * Retorna a nota da terceira avaliação / recuperação (A3). Valor 0.0 indica que
	 * a avaliação ainda não foi realizada.
	 * 
	 * @return nota A3 (0.0 se não realizada)
	 */
	public double getA3() {
		return a3;
	}

	/**
	 * Define a nota da terceira avaliação / recuperação.
	 * 
	 * @param a3 nota da A3 (usar 0.0 enquanto não realizada)
	 */
	public void setA3(double a3) {
		this.a3 = a3;
	}

	/**
	 * Retorna a média final do aluno na disciplina.
	 * 
	 * @return média calculada (0.0 a 10.0)
	 */
	public double getMedia() {
		return media;
	}

	/**
	 * Define a média final do aluno.
	 * 
	 * @param media média calculada externamente (pelo DAO ou pela tela)
	 */
	public void setMedia(double media) {
		this.media = media;
	}

	/**
	 * Retorna as faltas registradas no período da A1.
	 * 
	 * @return número de faltas da A1
	 */
	public int getFaltasA1() {
		return faltasA1;
	}

	/**
	 * Define as faltas do período da A1. Substitui o valor anterior — não acumula.
	 * 
	 * @param faltasA1 total de faltas no período da A1
	 */
	public void setFaltasA1(int faltasA1) {
		this.faltasA1 = faltasA1;
	}

	/**
	 * Retorna as faltas registradas no período da A2.
	 * 
	 * @return número de faltas da A2
	 */
	public int getFaltasA2() {
		return faltasA2;
	}

	/**
	 * Define as faltas do período da A2. Substitui o valor anterior — não acumula.
	 * 
	 * @param faltasA2 total de faltas no período da A2
	 */
	public void setFaltasA2(int faltasA2) {
		this.faltasA2 = faltasA2;
	}

	/**
	 * Retorna o total de faltas do aluno na disciplina no semestre.
	 *
	 * <p>
	 * Calculado automaticamente somando {@code faltasA1} e {@code faltasA2}. Usado
	 * no boletim, no PDF e em {@link #getSituacao()} — sem precisar alterar nenhuma
	 * dessas partes do sistema ao separar as faltas.
	 * </p>
	 *
	 * @return soma de faltasA1 + faltasA2
	 */
	public int getFaltas() {
		return faltasA1 + faltasA2;
	}

	/**
	 * Retorna o nome completo do aluno, obtido via JOIN.
	 * 
	 * @return nome do aluno
	 */
	public String getNomeAluno() {
		return nomeAluno;
	}

	/**
	 * Define o nome do aluno — chamado ao montar o objeto com resultado do JOIN.
	 * 
	 * @param nomeAluno nome vindo da tabela Aluno
	 */
	public void setNomeAluno(String nomeAluno) {
		this.nomeAluno = nomeAluno;
	}

	/**
	 * Retorna o RGM do aluno, obtido via JOIN.
	 * 
	 * @return RGM do aluno
	 */
	public String getRgmAluno() {
		return rgmAluno;
	}

	/**
	 * Define o RGM do aluno — chamado ao montar o objeto com resultado do JOIN.
	 * 
	 * @param rgmAluno RGM vindo da tabela Aluno
	 */
	public void setRgmAluno(String rgmAluno) {
		this.rgmAluno = rgmAluno;
	}

	/**
	 * Retorna o ID da disciplina vinculada a este registro.
	 * 
	 * @return disciplinaId (chave estrangeira referenciando a tabela Disciplina)
	 */
	public int getDisciplinaId() {
		return disciplinaId;
	}

	/**
	 * Define o ID da disciplina deste registro.
	 * 
	 * @param disciplinaId ID da disciplina na tabela Disciplina
	 */
	public void setDisciplinaId(int disciplinaId) {
		this.disciplinaId = disciplinaId;
	}

	// =========================================================
	// MÉTODOS DE NEGÓCIO
	// Lógica que vai além de apenas guardar dados —
	// aqui a classe "pensa" e toma decisões com base nos seus atributos.
	// =========================================================

	/**
	 * Calcula e retorna a situação acadêmica do aluno na disciplina.
	 *
	 * <p>
	 * As regras são aplicadas <b>em ordem de prioridade</b>:
	 * </p>
	 * <ol>
	 * <li><b>Reprovado por Falta</b> — verificado primeiro porque faltas >= 20
	 * reprovam independentemente das notas. Usa {@link #getFaltas()}, que soma
	 * faltasA1 + faltasA2 automaticamente.</li>
	 * <li><b>Aprovado</b> — média >= 6.0 e faltas dentro do limite.</li>
	 * <li><b>Em Recuperação</b> — média < 6.0 e A3 ainda não realizada (A3 == 0). O
	 * aluno ainda tem chance de melhorar a situação.</li>
	 * <li><b>Reprovado</b> — média < 6.0 após ter realizado a A3.</li>
	 * </ol>
	 *
	 * @return uma das strings: "Reprovado por Falta", "Aprovado", "Em Recuperação"
	 *         ou "Reprovado"
	 */
	public String getSituacao() {

		// 1ª verificação: total (faltasA1 + faltasA2) >= 20 reprovam antes de olhar as
		// notas
		if (getFaltas() >= 20) {
			return "Reprovado por Falta";
		}

		// 2ª verificação: média suficiente para aprovação direta
		if (media >= 6.0) {
			return "Aprovado";
		}

		// 3ª verificação: média insuficiente, mas A3 ainda não foi realizada
		// (A3 == 0 indica que a recuperação ainda está pendente)
		if (a3 == 0) {
			return "Em Recuperação";
		}

		// Caso final: realizou A3, mas ainda assim não atingiu média mínima
		return "Reprovado";
	}
}