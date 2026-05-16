package br.com.cadastroalunos.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.text.JTextComponent;

import br.com.cadastroalunos.dao.AlunoDAO;
import br.com.cadastroalunos.dao.CursoDAO;
import br.com.cadastroalunos.dao.NotaFaltaDAO;
import br.com.cadastroalunos.model.Aluno;
import br.com.cadastroalunos.model.Curso;
import br.com.cadastroalunos.model.Disciplina;
import br.com.cadastroalunos.model.NotaFalta;
import br.com.cadastroalunos.dao.DisciplinaDAO;

import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.PageSize;
import java.time.LocalDate;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.*;
import java.awt.event.*;

public class TelaPrincipal extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final int PAINEL_W = 740;
	private static final int PAINEL_H = 520;
	private static final int BTN_W = 170;
	private static final int BTN_H = 40;
	private static final int BTN_GAP = 10;
	private static final int MARGEM = 20;

	private final Color COR_CAMPO = new Color(198, 222, 241);
	private final Color COR_CAMPO_RO = new Color(190, 210, 230);

	// DAOs
	private final AlunoDAO alunoDAO = new AlunoDAO();
	private final CursoDAO cursoDAO = new CursoDAO();
	private final NotaFaltaDAO notaFaltaDAO = new NotaFaltaDAO();
	private final DisciplinaDAO disciplinaDAO = new DisciplinaDAO();

	// Estado
	private Aluno alunoAtual = null;
	private NotaFalta notaSelecionada = null;

	// --- Aba Dados Pessoais ---
	private JTextField txtRgm, txtNome, txtEmail, txtEndereco, txtMunicipio;
	private JFormattedTextField txtDataNasc, txtCpf, txtCelular;
	private JComboBox<String> cmbUf;

	// --- Aba Curso ---
	private JTextField txtCampus;
	private JComboBox<Curso> cmbCurso;
	private JRadioButton rbMatutino, rbVespertino, rbNoturno;
	private ButtonGroup grupoPeriodo;

	// --- Aba Notas e Faltas ---
	// txtFaltasA1 e txtFaltasA2 substituem o antigo txtFaltas único,
	// permitindo registrar e editar faltas por período de avaliação separadamente.
	private JTextField txtRgmNota, txtNomeNota, txtCursoNota;
	private JTextField txtFaltasA1, txtFaltasA2;
	private JLabel lblTotalFaltas;
	private JComboBox<String> cmbSemestre;
	private JComboBox<Disciplina> cmbDisciplina;
	private JTextField txtA1, txtA2, txtA3;
	private JLabel lblValorMedia, lblStatusMedia;
	private JButton btnLancarA3;

	// --- Aba Boletim ---
	private JTextField txtBoletimRgm, txtBoletimNome, txtBoletimCurso;
	private JTable tabelaBoletim;
	private DefaultTableModel modeloBoletim;

	// Constantes
	private static final String[] UFS = { "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG",
			"PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO" };
	private static final String[] SEMESTRES = { "2026-1" };

	// =========================================================
	// DIÁLOGOS PADRONIZADOS — sem duplicidade
	// =========================================================
	private void msgErro(String msg) {
		JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
	}

	private void msgSucesso(String msg) {
		JOptionPane.showMessageDialog(this, msg, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
	}

	private void msgAviso(String msg) {
		JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
	}

	// =========================================================
	public TelaPrincipal() {
		super("Sistema Academico - FATEC");
		configurarJanela();
		configurarMenu();
		configurarConteudo();
		carregarCursos();
		limparFormulario();
	}

	// =========================================================
	// JANELA
	// =========================================================
	private void configurarJanela() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(780, 620);
		setMinimumSize(new Dimension(780, 620));
		setResizable(false);
		setLocationRelativeTo(null);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(this);
		} catch (Exception ignored) {
		}
	}

	// =========================================================
	// MENU BAR
	// =========================================================
	private void configurarMenu() {
		JMenuBar bar = new JMenuBar();

		JMenu mAluno = new JMenu("Aluno");
		JMenuItem miSalvar = new JMenuItem("Salvar");
		miSalvar.addActionListener(e -> acaoSalvar());
		JMenuItem miAlterar = new JMenuItem("Alterar");
		miAlterar.addActionListener(e -> acaoAlterar());
		JMenuItem miConsultar = new JMenuItem("Consultar");
		miConsultar.addActionListener(e -> acaoConsultar());
		JMenuItem miExcluir = new JMenuItem("Excluir");
		miExcluir.addActionListener(e -> acaoExcluir());
		JMenuItem miSair = new JMenuItem("Sair");
		miSair.addActionListener(e -> System.exit(0));
		mAluno.add(miSalvar);
		mAluno.add(miAlterar);
		mAluno.add(miConsultar);
		mAluno.add(miExcluir);
		mAluno.addSeparator();
		mAluno.add(miSair);

		JMenu mNF = new JMenu("Notas e Faltas");
		JMenuItem miSalvarNF = new JMenuItem("Salvar");
		miSalvarNF.addActionListener(e -> acaoSalvarNota());
		JMenuItem miAlterarNF = new JMenuItem("Alterar");
		miAlterarNF.addActionListener(e -> acaoAlterarNota());
		JMenuItem miExcluirNF = new JMenuItem("Excluir");
		miExcluirNF.addActionListener(e -> acaoExcluirNota());
		JMenuItem miConsultarNF = new JMenuItem("Consultar");
		miConsultarNF.addActionListener(e -> acaoConsultarNota());
		mNF.add(miSalvarNF);
		mNF.add(miAlterarNF);
		mNF.add(miExcluirNF);
		mNF.add(miConsultarNF);

		JMenu mAjuda = new JMenu("Ajuda");
		JMenuItem miSobre = new JMenuItem("Sobre");
		miSobre.addActionListener(e -> mostrarSobre());
		mAjuda.add(miSobre);

		bar.add(mAluno);
		bar.add(mNF);
		bar.add(mAjuda);
		setJMenuBar(bar);
	}

	// =========================================================
	// CONTEUDO
	// =========================================================
	private void configurarConteudo() {
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Dados Pessoais", criarAbaDadosPessoais());
		tabs.addTab("Curso", criarAbaCurso());
		tabs.addTab("Notas e Faltas", criarAbaNotasFaltas());
		tabs.addTab("Boletim", criarAbaBoletim());
		getContentPane().add(tabs, BorderLayout.CENTER);
	}

	// =========================================================
	// ABA DADOS PESSOAIS
	// =========================================================
	private JPanel criarAbaDadosPessoais() {
		JPanel p = new JPanel(null);
		p.setPreferredSize(new Dimension(PAINEL_W, PAINEL_H));
		Font fonte = new Font("Arial", Font.PLAIN, 12);

		lbl(p, "RGM", MARGEM, 15, 80, 20);
		txtRgm = campo(p, MARGEM, 35, 120, 24, fonte);
		permitirSomenteInteiros(txtRgm);

		lbl(p, "Nome", 160, 15, 80, 20);
		txtNome = campo(p, 160, 35, PAINEL_W - 160 - MARGEM, 24, fonte);
		permitirSomenteLetras(txtNome);

		lbl(p, "Data de Nascimento", MARGEM, 75, 150, 20);
		txtDataNasc = fmt(p, "##/##/####", MARGEM, 95, 140, 24);
		estilizarCampo(txtDataNasc, fonte);

		lbl(p, "CPF", 190, 75, 80, 20);
		txtCpf = fmt(p, "###.###.###-##", 190, 95, 180, 24);
		estilizarCampo(txtCpf, fonte);

		lbl(p, "Email", MARGEM, 135, 80, 20);
		txtEmail = campo(p, MARGEM, 155, PAINEL_W - 2 * MARGEM, 24, fonte);

		lbl(p, "Endereço", MARGEM, 195, 100, 20);
		txtEndereco = campo(p, MARGEM, 215, PAINEL_W - 2 * MARGEM, 24, fonte);

		lbl(p, "Município", MARGEM, 255, 100, 20);
		txtMunicipio = campo(p, MARGEM, 275, 220, 24, fonte);

		lbl(p, "UF", 260, 255, 50, 20);
		cmbUf = new JComboBox<>(UFS);
		cmbUf.setBounds(260, 275, 80, 24);
		cmbUf.setBackground(COR_CAMPO);
		cmbUf.setFont(fonte);
		p.add(cmbUf);

		lbl(p, "Celular", 370, 255, 100, 20);
		txtCelular = fmt(p, "(##)#####-####", 370, 275, 170, 24);
		estilizarCampo(txtCelular, fonte);

		return p;
	}

	// =========================================================
	// ABA CURSO
	// =========================================================
	private JPanel criarAbaCurso() {
		JPanel p = new JPanel(null);
		p.setPreferredSize(new Dimension(PAINEL_W, PAINEL_H));
		Font fonte = new Font("Arial", Font.PLAIN, 12);

		lbl(p, "Curso", MARGEM, 20, 80, 20);
		cmbCurso = new JComboBox<>();
		cmbCurso.setBounds(MARGEM, 45, PAINEL_W - 2 * MARGEM, 24);
		cmbCurso.setBackground(COR_CAMPO);
		cmbCurso.setFont(fonte);
		cmbCurso.addActionListener(e -> atualizarDadosCurso());
		p.add(cmbCurso);

		lbl(p, "Campus", MARGEM, 90, 80, 20);
		txtCampus = new JTextField();
		txtCampus.setBounds(MARGEM, 115, 300, 24);
		txtCampus.setBackground(COR_CAMPO);
		txtCampus.setFont(fonte);
		txtCampus.setEditable(false);
		p.add(txtCampus);

		lbl(p, "Período", MARGEM, 160, 80, 20);
		grupoPeriodo = new ButtonGroup();
		rbMatutino = rb(p, "Matutino", MARGEM, 185, 110, 25, fonte);
		rbVespertino = rb(p, "Vespertino", MARGEM + 120, 185, 120, 25, fonte);
		rbNoturno = rb(p, "Noturno", MARGEM + 260, 185, 110, 25, fonte);
		rbNoturno.setSelected(true);
		grupoPeriodo.add(rbMatutino);
		grupoPeriodo.add(rbVespertino);
		grupoPeriodo.add(rbNoturno);

		JLabel lblAviso = new JLabel("* O curso não pode ser alterado após o cadastro.");
		lblAviso.setForeground(new Color(160, 60, 60));
		lblAviso.setFont(new Font("Arial", Font.ITALIC, 11));
		lblAviso.setBounds(MARGEM, 220, 450, 20);
		p.add(lblAviso);

		int bw = 180, row1Y = 255, row2Y = row1Y + BTN_H + 15;
		int col1X = 40, col2X = 280, col3X = 520;

		JButton btnSalvar = btn("Salvar", col1X, row1Y, bw, BTN_H);
		JButton btnConsultar = btn("Consultar", col2X, row1Y, bw, BTN_H);
		JButton btnAlterar = btn("Alterar", col3X, row1Y, bw, BTN_H);
		JButton btnExcluir = btn("Excluir", col1X, row2Y, bw, BTN_H);
		JButton btnLimpar = btn("Limpar", col2X, row2Y, bw, BTN_H);
		JButton btnSair = btn("Sair", col3X, row2Y, bw, BTN_H);

		btnSalvar.addActionListener(e -> acaoSalvar());
		btnConsultar.addActionListener(e -> acaoConsultar());
		btnAlterar.addActionListener(e -> acaoAlterar());
		btnExcluir.addActionListener(e -> acaoExcluir());
		btnLimpar.addActionListener(e -> limparFormulario());
		btnSair.addActionListener(e -> System.exit(0));
		btnSair.setBackground(new Color(200, 60, 60));
		btnSair.setForeground(Color.BLACK);
		btnSair.setFocusPainted(false);
		btnSair.setOpaque(true);

		p.add(btnSalvar);
		p.add(btnConsultar);
		p.add(btnAlterar);
		p.add(btnExcluir);
		p.add(btnLimpar);
		p.add(btnSair);
		return p;
	}

	// =========================================================
	// ABA NOTAS E FALTAS
	// =========================================================
	private JPanel criarAbaNotasFaltas() {
		JPanel p = new JPanel(null);
		p.setBackground(new Color(245, 245, 242));
		Font fonte = new Font("Arial", Font.PLAIN, 12);

		// Cabeçalho
		lbl(p, "RGM", MARGEM, 15, 40, 22);
		txtRgmNota = new JTextField();
		txtRgmNota.setBounds(MARGEM + 42, 15, 90, 25);
		txtRgmNota.setBackground(COR_CAMPO);
		txtRgmNota.setFont(fonte);
		permitirSomenteInteiros(txtRgmNota);
		p.add(txtRgmNota);

		txtNomeNota = new JTextField();
		txtNomeNota.setBounds(MARGEM + 42 + 90 + 8, 15, PAINEL_W - (MARGEM + 42 + 90 + 8) - MARGEM, 25);
		txtNomeNota.setEditable(false);
		txtNomeNota.setBackground(COR_CAMPO_RO);
		txtNomeNota.setFont(fonte);
		p.add(txtNomeNota);

		txtCursoNota = new JTextField();
		txtCursoNota.setBounds(MARGEM, 50, PAINEL_W - 2 * MARGEM, 25);
		txtCursoNota.setEditable(false);
		txtCursoNota.setBackground(COR_CAMPO_RO);
		txtCursoNota.setFont(fonte);
		p.add(txtCursoNota);

		JPanel sep = new JPanel();
		sep.setBackground(Color.LIGHT_GRAY);
		sep.setBounds(MARGEM, 88, PAINEL_W - 2 * MARGEM, 1);
		p.add(sep);

		// Disciplina
		lbl(p, "Disciplina", MARGEM, 100, 80, 25);
		cmbDisciplina = new JComboBox<>();
		cmbDisciplina.setBounds(MARGEM + 85, 100, PAINEL_W - (MARGEM + 85) - MARGEM, 25);
		cmbDisciplina.addActionListener(e -> carregarNotaPorDisciplina());
		p.add(cmbDisciplina);

		// Semestre
		lbl(p, "Semestre", MARGEM, 137, 70, 25);
		cmbSemestre = new JComboBox<>(SEMESTRES);
		cmbSemestre.setBounds(MARGEM + 72, 137, 100, 25);
		cmbSemestre.addActionListener(e -> carregarNotaPorDisciplina());
		p.add(cmbSemestre);

		// Cards A1 / A2 / A3 / Média
		int cardW = (PAINEL_W - 2 * MARGEM - 3 * BTN_GAP) / 4;
		int cardH = 85, cardY = 178;
		Font fonteBig = new Font("Arial", Font.BOLD, 26);

		JPanel cA1 = card(p, MARGEM, cardY, cardW, cardH);
		lbl(cA1, "A1", 10, 6, 30, 20);
		txtA1 = new JTextField();
		txtA1.setBounds(10, 32, cardW - 20, 30);
		txtA1.setFont(fonteBig);
		cA1.add(txtA1);

		int cx2 = MARGEM + cardW + BTN_GAP;
		JPanel cA2 = card(p, cx2, cardY, cardW, cardH);
		lbl(cA2, "A2", 10, 6, 30, 20);
		txtA2 = new JTextField();
		txtA2.setBounds(10, 32, cardW - 20, 30);
		txtA2.setFont(fonteBig);
		cA2.add(txtA2);

		int cx3 = cx2 + cardW + BTN_GAP;
		JPanel cA3 = card(p, cx3, cardY, cardW, cardH);
		lbl(cA3, "A3", 10, 6, 30, 20);
		txtA3 = new JTextField();
		txtA3.setEnabled(false);
		txtA3.setBounds(10, 32, cardW - 20, 30);
		txtA3.setFont(fonteBig);
		cA3.add(txtA3);

		int cx4 = cx3 + cardW + BTN_GAP;
		JPanel cMedia = new JPanel(null);
		cMedia.setBorder(BorderFactory.createLineBorder(new Color(110, 140, 70)));
		cMedia.setBackground(new Color(223, 231, 208));
		cMedia.setBounds(cx4, cardY, cardW, cardH);
		JLabel lblTitMedia = new JLabel("Média parcial");
		lblTitMedia.setBounds(10, 6, cardW - 12, 18);
		cMedia.add(lblTitMedia);
		lblValorMedia = new JLabel("0,0");
		lblValorMedia.setFont(new Font("Arial", Font.BOLD, 30));
		lblValorMedia.setBounds(10, 22, cardW - 12, 38);
		cMedia.add(lblValorMedia);
		lblStatusMedia = new JLabel("Sem cálculo");
		lblStatusMedia.setBounds(10, 62, cardW - 12, 16);
		cMedia.add(lblStatusMedia);
		p.add(cMedia);

		txtA1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				atualizarMediaParcial();
				verificarA3();
			}
		});
		txtA2.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				atualizarMediaParcial();
				verificarA3();
			}
		});
		permitirNotaValida(txtA1);
		permitirNotaValida(txtA2);
		permitirNotaValida(txtA3);

		// =========================================================
		// BLOCO DE FALTAS
		// =========================================================

		int faltasBoxY = cardY + cardH + 10;
		int faltasBoxH = 70;
		int faltasBoxW = PAINEL_W - 2 * MARGEM;

		JPanel faltasBox = new JPanel(null);
		faltasBox.setBorder(null);
		faltasBox.setBackground(new Color(245, 245, 242));
		faltasBox.setBounds(MARGEM, faltasBoxY, faltasBoxW, faltasBoxH);
		p.add(faltasBox);

		int campoW = 80, campoH = 26;

		JLabel lblFA1 = new JLabel("Faltas A1");
		lblFA1.setFont(fonte);
		lblFA1.setBounds(0, 6, campoW, 18);
		faltasBox.add(lblFA1);

		txtFaltasA1 = new JTextField();
		txtFaltasA1.setBounds(0, 26, campoW, campoH);
		txtFaltasA1.setBackground(COR_CAMPO);
		txtFaltasA1.setFont(fonte);
		faltasBox.add(txtFaltasA1);
		permitirSomenteInteiros(txtFaltasA1);
		txtFaltasA1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				atualizarLabelTotalFaltas();
			}
		});

		int startX2 = campoW + 10;
		JLabel lblFA2 = new JLabel("Faltas A2");
		lblFA2.setFont(fonte);
		lblFA2.setBounds(startX2, 6, campoW, 18);
		faltasBox.add(lblFA2);

		txtFaltasA2 = new JTextField();
		txtFaltasA2.setBounds(startX2, 26, campoW, campoH);
		txtFaltasA2.setBackground(COR_CAMPO);
		txtFaltasA2.setFont(fonte);
		faltasBox.add(txtFaltasA2);
		permitirSomenteInteiros(txtFaltasA2);
		txtFaltasA2.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				atualizarLabelTotalFaltas();
			}
		});

		lblTotalFaltas = new JLabel("Total: 0  |  Limite: 20");
		lblTotalFaltas.setFont(new Font("Arial", Font.PLAIN, 11));
		lblTotalFaltas.setForeground(new Color(80, 80, 80));
		lblTotalFaltas.setBounds(startX2 + campoW + 15, 30, 160, 18);
		faltasBox.add(lblTotalFaltas);

		// Aviso sobre A3
		int avisoY = faltasBoxY + faltasBoxH + 8;
		JPanel aviso = new JPanel(null);
		aviso.setBackground(new Color(234, 228, 214));
		aviso.setBounds(MARGEM, avisoY, PAINEL_W - 2 * MARGEM, 26);
		JLabel textoAviso = new JLabel("Média parcial abaixo de 6,0 — o campo A3 será liberado após salvar A1 e A2.");
		textoAviso.setBounds(8, 4, PAINEL_W - 2 * MARGEM - 16, 18);
		textoAviso.setFont(new Font("Arial", Font.PLAIN, 11));
		aviso.add(textoAviso);
		p.add(aviso);

		// =========================================================
		// BOTÕES — distribuídos proporcionalmente em 3 colunas
		// =========================================================
		int bw = BTN_W;
		int espaco = (PAINEL_W - 3 * bw) / 4;
		int col1X = espaco;
		int col2X = espaco * 2 + bw;
		int col3X = espaco * 3 + bw * 2;

		int row1Y = avisoY + 36;
		int row2Y = row1Y + BTN_H + BTN_GAP;
		int row3Y = row2Y + BTN_H + BTN_GAP;

		// Linha 1 — Consultar e Limpar (centralizados nas colunas 1 e 2)
		JButton btnConsultar = btn("Consultar", col1X, row1Y, bw, BTN_H);
		btnConsultar.addActionListener(e -> acaoConsultarNota());
		p.add(btnConsultar);

		JButton btnLimpar = btn("Limpar", col2X, row1Y, bw, BTN_H);
		btnLimpar.addActionListener(e -> limparAbaNotas());
		p.add(btnLimpar);

		// Linha 2 — Salvar A1, Salvar A2, Lançar A3
		JButton btnSalvarA1 = btn("Salvar A1", col1X, row2Y, bw, BTN_H);
		btnSalvarA1.addActionListener(e -> acaoSalvarNotaA1());
		p.add(btnSalvarA1);

		JButton btnSalvarA2 = btn("Salvar A2", col2X, row2Y, bw, BTN_H);
		btnSalvarA2.addActionListener(e -> acaoSalvarNotaA2());
		p.add(btnSalvarA2);

		// JButton btnLancarA3 = btn("Lançar A3", col3X, row2Y, bw, BTN_H);
		btnLancarA3 = btn("Lançar A3", col3X, row2Y, bw, BTN_H);
		btnLancarA3.setEnabled(false);
		btnLancarA3.addActionListener(e -> acaoSalvarNota());
		p.add(btnLancarA3);

		// Linha 3 — Alterar, Excluir, Sair
		JButton btnAlterar = btn("Alterar", col1X, row3Y, bw, BTN_H);
		btnAlterar.addActionListener(e -> acaoAlterarNota());
		p.add(btnAlterar);

		JButton btnExcluir = btn("Excluir", col2X, row3Y, bw, BTN_H);
		btnExcluir.addActionListener(e -> acaoExcluirNota());
		p.add(btnExcluir);

		JButton btnSair = btn("Sair", col3X, row3Y, bw, BTN_H);
		btnSair.addActionListener(e -> System.exit(0));
		btnSair.setBackground(new Color(200, 60, 60));
		btnSair.setForeground(Color.BLACK);
		btnSair.setFocusPainted(false);
		btnSair.setOpaque(true);
		p.add(btnSair);

		p.setPreferredSize(new Dimension(PAINEL_W, row3Y + BTN_H + MARGEM));

		JScrollPane scroll = new JScrollPane(p);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		scroll.setBorder(null);

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.add(scroll, BorderLayout.CENTER);
		return wrapper;
	}

	// =========================================================
	// ABA BOLETIM
	// =========================================================
	private JPanel criarAbaBoletim() {
		JPanel p = new JPanel(null);
		p.setPreferredSize(new Dimension(PAINEL_W, PAINEL_H));
		Font fonteTitulo = new Font("Arial", Font.BOLD, 16);
		Font fonteNormal = new Font("Arial", Font.PLAIN, 12);

		JLabel titulo = new JLabel("BOLETIM ACADÊMICO");
		titulo.setFont(fonteTitulo);
		titulo.setHorizontalAlignment(SwingConstants.CENTER);
		titulo.setBounds(MARGEM, 15, PAINEL_W - 2 * MARGEM, 30);
		p.add(titulo);

		lbl(p, "RGM", MARGEM, 58, 80, 20);
		txtBoletimRgm = new JTextField();
		txtBoletimRgm.setBounds(MARGEM, 78, 130, 24);
		txtBoletimRgm.setBackground(COR_CAMPO);
		txtBoletimRgm.setEditable(false);
		txtBoletimRgm.setFont(fonteNormal);
		p.add(txtBoletimRgm);

		lbl(p, "Nome", MARGEM + 148, 58, 80, 20);
		txtBoletimNome = new JTextField();
		txtBoletimNome.setBounds(MARGEM + 148, 78, PAINEL_W - (MARGEM + 148) - MARGEM, 24);
		txtBoletimNome.setBackground(COR_CAMPO);
		txtBoletimNome.setEditable(false);
		txtBoletimNome.setFont(fonteNormal);
		p.add(txtBoletimNome);

		lbl(p, "Curso", MARGEM, 115, 80, 20);
		txtBoletimCurso = new JTextField();
		txtBoletimCurso.setBounds(MARGEM, 135, PAINEL_W - 2 * MARGEM, 24);
		txtBoletimCurso.setBackground(COR_CAMPO);
		txtBoletimCurso.setEditable(false);
		txtBoletimCurso.setFont(fonteNormal);
		p.add(txtBoletimCurso);

		String[] colunas = { "Disciplina", "Semestre", "A1", "A2", "A3", "Média", "Faltas", "Situação" };
		modeloBoletim = new DefaultTableModel(colunas, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		tabelaBoletim = new JTable(modeloBoletim);
		tabelaBoletim.getTableHeader().setReorderingAllowed(false);
		tabelaBoletim.setRowHeight(25);
		tabelaBoletim.setFont(fonteNormal);
		tabelaBoletim.setRowSelectionAllowed(false);
		tabelaBoletim.setCellSelectionEnabled(false);
		tabelaBoletim.setFocusable(false);
		tabelaBoletim.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
				Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
				comp.setBackground(Color.WHITE);
				if (c == 5) {
					try {
						double n = Double.parseDouble(v.toString());
						comp.setForeground(n < 6 ? new Color(139, 0, 40) : new Color(110, 140, 70));
					} catch (Exception ex) {
						comp.setForeground(Color.BLACK);
					}
				} else {
					comp.setForeground(Color.BLACK);
				}
				return comp;
			}
		});

		int tabelaY = 170, tabelaH = PAINEL_H - tabelaY - BTN_H - 30;
		JScrollPane scroll = new JScrollPane(tabelaBoletim);
		scroll.setBounds(MARGEM, tabelaY, PAINEL_W - 2 * MARGEM, tabelaH);
		p.add(scroll);

		int btnPdfX = (PAINEL_W - 200) / 2;
		JButton btnPdf = btn("Exportar PDF", btnPdfX, tabelaY + tabelaH + 10, 200, BTN_H);
		btnPdf.addActionListener(e -> gerarPDFBoletim());
		p.add(btnPdf);

		return p;
	}

	// =========================================================
	// AÇÕES CRUD ALUNO
	// =========================================================
	private void acaoSalvar() {
		try {
			if (!validarCamposAluno())
				return;

			String data = txtDataNasc.getText().trim();
			int ano = Integer.parseInt(data.substring(6, 10));
			if (ano < 1900 || ano > LocalDate.now().getYear()) {
				msgErro("Ano de nascimento inválido.");
				txtDataNasc.requestFocus();
				return;
			}
			if (alunoDAO.existeRgm(txtRgm.getText().trim())) {
				msgErro("Já existe um aluno cadastrado com este RGM.");
				txtRgm.requestFocus();
				return;
			}
			if (alunoDAO.cpfJaExiste(txtCpf.getText().trim())) {
				msgErro("Já existe um aluno com este CPF.");
				txtCpf.requestFocus();
				return;
			}
			Aluno aluno = construirAlunoDoForm();
			alunoDAO.salvar(aluno);
			alunoAtual = aluno;
			msgSucesso("Aluno salvo com sucesso!");
			limparFormulario();
		} catch (Exception ex) {
			msgErro("Erro ao salvar aluno:\n" + ex.getMessage());
		}
	}

	private void acaoAlterar() {
		try {
			if (!validarCamposAluno())
				return;
			if (alunoAtual == null) {
				msgAviso("Consulte um aluno primeiro!");
				return;
			}

			String data = txtDataNasc.getText().trim();
			int ano = Integer.parseInt(data.substring(6, 10));
			if (ano < 1900 || ano > LocalDate.now().getYear()) {
				msgErro("Ano de nascimento inválido.");
				txtDataNasc.requestFocus();
				return;
			}
			alunoAtual.setRgm(txtRgm.getText().trim());
			alunoAtual.setNome(txtNome.getText().trim());
			alunoAtual.setDataNascimento(txtDataNasc.getText().trim());
			alunoAtual.setCpf(txtCpf.getText().trim());
			alunoAtual.setEmail(txtEmail.getText().trim());
			alunoAtual.setEndereco(txtEndereco.getText().trim());
			alunoAtual.setMunicipio(txtMunicipio.getText().trim());
			alunoAtual.setUf(cmbUf.getSelectedItem().toString());
			alunoAtual.setCelular(txtCelular.getText().trim());
			alunoDAO.alterar(alunoAtual);
			msgSucesso("Aluno alterado com sucesso!");
		} catch (Exception ex) {
			msgErro("Erro ao alterar aluno:\n" + ex.getMessage());
		}
	}

	private void acaoConsultar() {
		try {
			String rgm = txtRgm.getText().trim();
			if (rgm.isEmpty()) {
				msgAviso("Informe o RGM.");
				return;
			}
			Aluno aluno = alunoDAO.buscarPorRgm(rgm);
			if (aluno == null) {
				msgAviso("Aluno não encontrado.");
				return;
			}
			alunoAtual = aluno;
			preencherCamposAluno(aluno);
			cmbCurso.setEnabled(false);
			carregarDadosAlunoNotas();
			atualizarTabelaBoletim();
			msgSucesso("Aluno carregado com sucesso!");
		} catch (Exception ex) {
			msgErro("Erro ao consultar aluno:\n" + ex.getMessage());
		}
	}

	private void acaoExcluir() {
		try {
			String rgm = txtRgm.getText().trim();
			if (rgm.isEmpty()) {
				msgAviso("Informe o RGM.");
				return;
			}
			int r = JOptionPane.showConfirmDialog(this, "Deseja realmente excluir este aluno?", "Confirmação",
					JOptionPane.YES_NO_OPTION);
			if (r != JOptionPane.YES_OPTION)
				return;
			alunoDAO.excluir(rgm);
			msgSucesso("Aluno excluído com sucesso!");
			limparFormulario();
		} catch (Exception ex) {
			msgErro("Erro ao excluir aluno:\n" + ex.getMessage());
		}
	}

	// =========================================================
	// AÇÕES NOTAS E FALTAS
	// =========================================================
	private void acaoConsultarNota() {
		String rgm = txtRgmNota.getText().trim();
		if (rgm.isEmpty()) {
			msgAviso("Digite o RGM do aluno.");
			return;
		}
		try {
			Aluno aluno = alunoDAO.buscarPorRgm(rgm);
			if (aluno == null) {
				msgAviso("Aluno não encontrado.");
				return;
			}
			alunoAtual = aluno;
			carregarDadosAlunoNotas();
			atualizarTabelaBoletim();
			msgSucesso("Aluno carregado com sucesso!");
		} catch (Exception ex) {
			msgErro("Erro ao consultar aluno:\n" + ex.getMessage());
		}
	}

	private void acaoSalvarNotaA1() {
		try {
			if (alunoAtual == null) {
				msgAviso("Consulte um aluno primeiro.");
				return;
			}
			Disciplina disc = (Disciplina) cmbDisciplina.getSelectedItem();
			if (disc == null)
				return;
			if (txtA1.getText().trim().isEmpty()) {
				msgAviso("Informe a nota A1.");
				return;
			}
			if (txtFaltasA1.getText().trim().isEmpty()) {
				msgAviso("Informe as faltas da A1.");
				return;
			}

			double a1 = Double.parseDouble(txtA1.getText().replace(",", "."));
			if (a1 < 0 || a1 > 10) {
				msgErro("A nota A1 deve ser entre 0 e 10.");
				return;
			}

			// Lê as faltas do campo específico da A1 (substitui — não acumula)
			int faltasA1 = Integer.parseInt(txtFaltasA1.getText().trim());
			String sem = cmbSemestre.getSelectedItem().toString();

			NotaFalta nf = notaFaltaDAO.buscarPorAlunoDisciplinaSemestre(alunoAtual.getId(), disc.getId(), sem);
			if (nf == null) {
				nf = new NotaFalta();
				nf.setAlunoId(alunoAtual.getId());
				nf.setDisciplinaId(disc.getId());
				nf.setDisciplina(disc.getNome());
				nf.setSemestre(sem);
			}
			nf.setA1(a1);
			nf.setFaltasA1(faltasA1); // grava somente as faltas da A1
			nf.setMedia(nf.getA2() > 0 ? (a1 + nf.getA2()) / 2.0 : 0);

			if (nf.getId() == 0)
				notaFaltaDAO.salvar(nf);
			else
				notaFaltaDAO.alterar(nf);

			txtA1.setEditable(false);
			atualizarLabelTotalFaltas();
			atualizarTabelaBoletim();
			msgSucesso("A1 salva com sucesso!");
		} catch (Exception ex) {
			msgErro("Erro ao salvar A1:\n" + ex.getMessage());
		}
	}

	private void acaoSalvarNotaA2() {
		try {
			if (alunoAtual == null)
				return;
			Disciplina disc = (Disciplina) cmbDisciplina.getSelectedItem();
			String sem = cmbSemestre.getSelectedItem().toString();
			if (txtA2.getText().trim().isEmpty()) {
				msgAviso("Informe a nota A2.");
				return;
			}
			if (txtFaltasA2.getText().trim().isEmpty()) {
				msgAviso("Informe as faltas da A2.");
				return;
			}

			double a2 = Double.parseDouble(txtA2.getText().replace(",", "."));
			if (a2 < 0 || a2 > 10) {
				msgErro("A nota A2 deve ser entre 0 e 10.");
				return;
			}

			// Lê as faltas do campo específico da A2 (substitui — não acumula)
			int faltasA2 = Integer.parseInt(txtFaltasA2.getText().trim());

			NotaFalta nf = notaFaltaDAO.buscarPorAlunoDisciplinaSemestre(alunoAtual.getId(), disc.getId(), sem);
			if (nf == null) {
				msgAviso("Salve a A1 primeiro.");
				return;
			}

			nf.setA2(a2);
			nf.setFaltasA2(faltasA2); // grava somente as faltas da A2
			nf.setMedia(nf.getA1() > 0 ? (nf.getA1() + a2) / 2.0 : 0);

			notaFaltaDAO.alterar(nf);

			txtA2.setEditable(false);
			verificarA3();
			atualizarMediaParcial();
			atualizarLabelTotalFaltas();
			atualizarTabelaBoletim();
			msgSucesso("A2 salva com sucesso!");
		} catch (Exception ex) {
			msgErro("Erro ao salvar A2:\n" + ex.getMessage());
		}
	}

	private void acaoSalvarNota() {
		try {
			if (!validarCamposNota())
				return;
			if (alunoAtual == null)
				return;
			Disciplina disc = (Disciplina) cmbDisciplina.getSelectedItem();
			String sem = cmbSemestre.getSelectedItem().toString();

			double a1 = Double.parseDouble(txtA1.getText().replace(",", "."));
			double a2 = Double.parseDouble(txtA2.getText().replace(",", "."));
			double a3 = txtA3.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtA3.getText().replace(",", "."));

			if (a1 < 0 || a1 > 10) {
				msgErro("A nota A1 deve ser entre 0 e 10.");
				return;
			}
			if (a2 < 0 || a2 > 10) {
				msgErro("A nota A2 deve ser entre 0 e 10.");
				return;
			}
			if (a3 < 0 || a3 > 10) {
				msgErro("A nota A3 deve ser entre 0 e 10.");
				return;
			}

			NotaFalta nf = notaFaltaDAO.buscarPorAlunoDisciplinaSemestre(alunoAtual.getId(), disc.getId(), sem);
			boolean novo = nf == null;
			if (novo) {
				nf = new NotaFalta();
				nf.setAlunoId(alunoAtual.getId());
				nf.setDisciplinaId(disc.getId());
				nf.setDisciplina(disc.getNome());
				nf.setSemestre(sem);
			}
			nf.setA1(a1);
			nf.setA2(a2);
			nf.setA3(a3);
			// Faltas A1 e A2 já foram salvas nos passos anteriores — não altera aqui
			nf.setMedia(calcularMedia(a1, a2, a3));

			if (novo)
				notaFaltaDAO.salvar(nf);
			else
				notaFaltaDAO.alterar(nf);

			txtA1.setEditable(false);
			txtA2.setEditable(false);
			txtA3.setEnabled(false);

			atualizarTabelaBoletim();
			atualizarMediaParcial();
			msgSucesso("A3 lançada com sucesso!");
		} catch (Exception ex) {
			msgErro("Erro ao lançar A3:\n" + ex.getMessage());
		}
	}

	private void acaoAlterarNota() {
		try {
			if (alunoAtual == null) {
				msgAviso("Consulte um aluno primeiro.");
				return;
			}
			carregarNotaPorDisciplina();
			if (notaSelecionada == null) {
				msgAviso("Nenhuma nota encontrada para esta disciplina/semestre.");
				return;
			}
			// Libera os campos de nota e faltas para reedição
			txtA1.setEditable(true);
			txtA2.setEditable(true);
			txtFaltasA1.setEditable(true);
			txtFaltasA2.setEditable(true);
			verificarA3();
			msgAviso("Campos liberados para edição.\nAjuste os valores e clique em Salvar A1 ou Salvar A2.");
		} catch (Exception ex) {
			msgErro("Erro ao preparar alteração:\n" + ex.getMessage());
		}
	}

	private void acaoExcluirNota() {
		try {
			if (alunoAtual == null) {
				msgAviso("Consulte um aluno primeiro.");
				return;
			}

			carregarNotaPorDisciplina();

			if (notaSelecionada == null) {
				msgAviso("Nenhuma nota encontrada para esta disciplina/semestre.");
				return;
			}

			int resp = JOptionPane.showConfirmDialog(this,
					"Deseja excluir a nota da disciplina \"" + notaSelecionada.getDisciplina() + "\"?",
					"Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (resp != JOptionPane.YES_OPTION)
				return;

			notaFaltaDAO.excluir(notaSelecionada.getId());
			notaSelecionada = null;
			txtA1.setText("");
			txtA2.setText("");
			txtA3.setText("");
			txtFaltasA1.setText("");
			txtFaltasA2.setText("");

			txtA1.setEditable(true);
			txtA2.setEditable(true);
			txtFaltasA1.setEditable(true);
			txtFaltasA2.setEditable(true);
			txtA3.setEnabled(false);

			atualizarLabelTotalFaltas();
			atualizarTabelaBoletim();
			msgSucesso("Nota excluída com sucesso!");
		} catch (Exception ex) {
			msgErro("Erro ao excluir nota:\n" + ex.getMessage());
		}
	}

	// =========================================================
	// HELPERS — NOTAS
	// =========================================================

	/**
	 * Atualiza o label "Total: X | Limite: 20" em tempo real, somando os valores
	 * digitados em txtFaltasA1 e txtFaltasA2. Se um dos campos estiver vazio,
	 * considera 0.
	 */
	private void atualizarLabelTotalFaltas() {
		try {
			int fa1 = txtFaltasA1.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtFaltasA1.getText().trim());
			int fa2 = txtFaltasA2.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtFaltasA2.getText().trim());
			int total = fa1 + fa2;
			// Destaca em vermelho se o aluno já atingiu ou ultrapassou o limite
			lblTotalFaltas.setForeground(total >= 20 ? new Color(180, 0, 0) : new Color(80, 80, 80));
			lblTotalFaltas.setText("Total: " + total + "  |  Limite: 20");
		} catch (NumberFormatException ex) {
			lblTotalFaltas.setText("Total: ?  |  Limite: 20");
		}
	}

	private void carregarNotaPorDisciplina() {
		try {
			// Se não há aluno, limpa tudo e libera os campos para digitação futura
			if (alunoAtual == null) {
				txtA1.setText("");
				txtA2.setText("");
				txtA3.setText("");
				txtFaltasA1.setText("");
				txtFaltasA2.setText("");
				txtA1.setEditable(true);
				txtA2.setEditable(true);
				txtFaltasA1.setEditable(true);
				txtFaltasA2.setEditable(true);
				atualizarLabelTotalFaltas();
				return;
			}

			Disciplina disc = (Disciplina) cmbDisciplina.getSelectedItem();
			if (disc == null)
				return;

			String semestre = cmbSemestre.getSelectedItem().toString();
			List<NotaFalta> lista = notaFaltaDAO.listarPorAluno(alunoAtual.getId());
			notaSelecionada = null;

			for (NotaFalta nf : lista) {
				if (nf.getDisciplinaId() == disc.getId() && nf.getSemestre().equals(semestre)) {
					notaSelecionada = nf;
					txtA1.setText(String.valueOf(nf.getA1()));
					txtA2.setText(String.valueOf(nf.getA2()));
					txtA3.setText(nf.getA3() > 0 ? String.valueOf(nf.getA3()) : "");
					txtFaltasA1.setText(String.valueOf(nf.getFaltasA1()));
					txtFaltasA2.setText(String.valueOf(nf.getFaltasA2()));
					atualizarLabelTotalFaltas();
					atualizarMediaParcial();
					verificarA3();
					// Registro existente: trava até clicar em Alterar
					txtA1.setEditable(false);
					txtA2.setEditable(false);
					txtFaltasA1.setEditable(false);
					txtFaltasA2.setEditable(false);
					return;
				}
			}

			// Nenhum registro encontrado — libera todos os campos para novo lançamento
			txtA1.setText("");
			txtA2.setText("");
			txtA3.setText("");
			txtFaltasA1.setText("");
			txtFaltasA2.setText("");
			txtA1.setEditable(true);
			txtA2.setEditable(true);
			txtFaltasA1.setEditable(true);
			txtFaltasA2.setEditable(true);
			atualizarLabelTotalFaltas();

		} catch (Exception ex) {
			msgErro("Erro ao carregar nota:\n" + ex.getMessage());
		}
	}

	private void atualizarDadosCurso() {
		Curso c = (Curso) cmbCurso.getSelectedItem();
		if (c == null)
			return;
		txtCampus.setText(c.getCampus() != null ? c.getCampus() : "");
		rbMatutino.setSelected(false);
		rbVespertino.setSelected(false);
		rbNoturno.setSelected(false);
		if (c.getPeriodo() != null) {
			switch (c.getPeriodo()) {
			case "Matutino":
				rbMatutino.setSelected(true);
				break;
			case "Vespertino":
				rbVespertino.setSelected(true);
				break;
			case "Noturno":
				rbNoturno.setSelected(true);
				break;
			}
		}
	}

	private void carregarCursos() {
		try {
			cmbCurso.removeAllItems();
			cmbCurso.addItem(null);
			for (Curso c : cursoDAO.listarCursosUnicos())
				cmbCurso.addItem(c);
			cmbCurso.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList l, Object v, int i, boolean s, boolean f) {
					super.getListCellRendererComponent(l, v, i, s, f);
					setText(v == null ? "Selecione um curso" : v.toString());
					return this;
				}
			});
			cmbCurso.setSelectedIndex(0);
		} catch (SQLException ex) {
			msgErro("Erro ao carregar cursos:\n" + ex.getMessage());
		}
	}

	// =========================================================
	// TABELA DO BOLETIM
	// Usa nf.getFaltas() que retorna faltasA1 + faltasA2 automaticamente.
	// =========================================================
	private void atualizarTabelaBoletim() {
		try {
			modeloBoletim.setRowCount(0);
			if (alunoAtual == null)
				return;
			for (NotaFalta nf : notaFaltaDAO.listarPorAluno(alunoAtual.getId())) {
				modeloBoletim.addRow(new Object[] { nf.getDisciplina(), nf.getSemestre(), nf.getA1(), nf.getA2(),
						nf.getA3() == 0 ? "-" : nf.getA3(), nf.getMedia(), nf.getFaltas(), nf.getSituacao() });
			}
			txtBoletimRgm.setText(alunoAtual.getRgm());
			txtBoletimNome.setText(alunoAtual.getNome());
			txtBoletimCurso.setText(alunoAtual.getNomeCurso());
		} catch (Exception ex) {
			msgErro("Erro ao carregar boletim:\n" + ex.getMessage());
		}
	}

	// =========================================================
	// PDF
	// =========================================================
	private void gerarPDFBoletim() {
		try {
			if (alunoAtual == null) {
				msgAviso("Consulte um aluno primeiro.");
				return;
			}
			String nomeArquivo = "Boletim_" + alunoAtual.getRgm() + ".pdf";
			Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
			PdfWriter.getInstance(doc, new FileOutputStream(nomeArquivo));
			doc.open();

			BaseColor azulEscuro = new BaseColor(44, 62, 80), azulClaro = new BaseColor(230, 240, 255);
			BaseColor verde = new BaseColor(198, 239, 206), vermelho = new BaseColor(255, 199, 206);
			BaseColor cinza = new BaseColor(245, 245, 245);

			com.itextpdf.text.Font fTit = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.WHITE);
			com.itextpdf.text.Font fNorm = FontFactory.getFont(FontFactory.HELVETICA, 12);
			com.itextpdf.text.Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
			com.itextpdf.text.Font fCab = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);

			PdfPTable cab = new PdfPTable(1);
			cab.setWidthPercentage(100);
			PdfPCell ct = new PdfPCell(new Phrase("BOLETIM ACADÊMICO", fTit));
			ct.setHorizontalAlignment(Element.ALIGN_CENTER);
			ct.setBackgroundColor(azulEscuro);
			ct.setPadding(15);
			ct.setBorder(0);
			cab.addCell(ct);
			doc.add(cab);
			doc.add(new Paragraph(" "));

			PdfPTable dadosA = new PdfPTable(2);
			dadosA.setWidthPercentage(100);
			dadosA.setWidths(new float[] { 1f, 2f });
			addInfoCell(dadosA, "RGM:", alunoAtual.getRgm(), fBold, fNorm, azulClaro);
			addInfoCell(dadosA, "Nome:", alunoAtual.getNome(), fBold, fNorm, azulClaro);
			addInfoCell(dadosA, "Curso:", alunoAtual.getNomeCurso() != null ? alunoAtual.getNomeCurso() : "", fBold,
					fNorm, azulClaro);
			doc.add(dadosA);
			doc.add(new Paragraph(" "));

			PdfPTable tab = new PdfPTable(4);
			tab.setWidthPercentage(100);
			tab.setSpacingBefore(10);
			tab.setWidths(new float[] { 4f, 1.5f, 1.5f, 2f });
			for (String col : new String[] { "Disciplina", "Nota", "Faltas", "Situação" }) {
				PdfPCell ch = new PdfPCell(new Phrase(col, fCab));
				ch.setBackgroundColor(azulEscuro);
				ch.setHorizontalAlignment(Element.ALIGN_CENTER);
				ch.setPadding(8);
				tab.addCell(ch);
			}
			List<NotaFalta> lista = notaFaltaDAO.listarPorAluno(alunoAtual.getId());
			double soma = 0;
			boolean par = false;
			for (NotaFalta nf : lista) {
				BaseColor cor = par ? cinza : BaseColor.WHITE;
				PdfPCell cD = new PdfPCell(new Phrase(nf.getDisciplina(), fNorm));
				cD.setBackgroundColor(cor);
				cD.setPadding(6);
				PdfPCell cN = new PdfPCell(new Phrase(String.format("%.1f", nf.getMedia()), fNorm));
				cN.setHorizontalAlignment(Element.ALIGN_CENTER);
				cN.setBackgroundColor(cor);
				cN.setPadding(6);
				// nf.getFaltas() retorna faltasA1 + faltasA2 automaticamente
				PdfPCell cF = new PdfPCell(new Phrase(String.valueOf(nf.getFaltas()), fNorm));
				cF.setHorizontalAlignment(Element.ALIGN_CENTER);
				cF.setBackgroundColor(cor);
				cF.setPadding(6);
				PdfPCell cS = new PdfPCell(new Phrase(nf.getSituacao(), fBold));
				cS.setHorizontalAlignment(Element.ALIGN_CENTER);
				cS.setPadding(6);
				cS.setBackgroundColor(nf.getSituacao().equalsIgnoreCase("Aprovado") ? verde : vermelho);
				tab.addCell(cD);
				tab.addCell(cN);
				tab.addCell(cF);
				tab.addCell(cS);
				soma += nf.getMedia();
				par = !par;
			}
			doc.add(tab);
			doc.add(new Paragraph(" "));
			double mg = lista.isEmpty() ? 0 : soma / lista.size();
			Paragraph pm = new Paragraph("Média Geral: " + String.format("%.1f", mg), fBold);
			pm.setAlignment(Element.ALIGN_RIGHT);
			doc.add(pm);
			doc.add(new Paragraph(" "));
			doc.add(new Paragraph("Emitido em: " + LocalDate.now(), fNorm));
			doc.add(new Paragraph(" "));
			Paragraph ass = new Paragraph("\n\n__________________________________\nSecretaria Acadêmica", fNorm);
			ass.setAlignment(Element.ALIGN_CENTER);
			doc.add(ass);
			doc.close();
			java.awt.Desktop.getDesktop().open(new java.io.File(nomeArquivo));
			msgSucesso("PDF gerado com sucesso!");
		} catch (Exception ex) {
			msgErro("Erro ao gerar PDF:\n" + ex.getMessage());
		}
	}

	private void addInfoCell(PdfPTable t, String label, String valor, com.itextpdf.text.Font fBold,
			com.itextpdf.text.Font fNorm, BaseColor bg) {
		PdfPCell c1 = new PdfPCell(new Phrase(label, fBold));
		c1.setBackgroundColor(bg);
		t.addCell(c1);
		t.addCell(new PdfPCell(new Phrase(valor, fNorm)));
	}

	// =========================================================
	// HELPERS — DADOS ALUNO
	// =========================================================
	private Aluno construirAlunoDoForm() {
		Aluno a = new Aluno();
		a.setRgm(txtRgm.getText().trim());
		a.setNome(txtNome.getText().trim());
		a.setDataNascimento(txtDataNasc.getText().trim());
		a.setCpf(txtCpf.getText().trim());
		a.setEmail(txtEmail.getText().trim());
		a.setEndereco(txtEndereco.getText().trim());
		a.setMunicipio(txtMunicipio.getText().trim());
		a.setUf(cmbUf.getSelectedItem().toString());
		a.setCelular(txtCelular.getText().trim());
		Curso c = (Curso) cmbCurso.getSelectedItem();
		if (c != null)
			a.setCursoId(c.getId());
		return a;
	}

	private void preencherCamposAluno(Aluno aluno) {
		txtRgm.setText(aluno.getRgm());
		txtNome.setText(aluno.getNome());
		txtDataNasc.setText(aluno.getDataNascimento());
		txtCpf.setText(aluno.getCpf());
		txtEmail.setText(aluno.getEmail());
		txtEndereco.setText(aluno.getEndereco());
		txtMunicipio.setText(aluno.getMunicipio());
		cmbUf.setSelectedItem(aluno.getUf());
		txtCelular.setText(aluno.getCelular());
		for (int i = 0; i < cmbCurso.getItemCount(); i++) {
			Curso c = cmbCurso.getItemAt(i);
			if (c != null && c.getId() == aluno.getCursoId()) {
				cmbCurso.setSelectedIndex(i);
				break;
			}
		}
		atualizarDadosCurso();
	}

	private void carregarDadosAlunoNotas() {
		if (alunoAtual == null)
			return;
		txtRgmNota.setText(alunoAtual.getRgm());
		txtNomeNota.setText(alunoAtual.getNome());
		txtCursoNota.setText(alunoAtual.getNomeCurso());
		carregarDisciplinasPorAluno();
	}

	private void carregarDisciplinasPorAluno() {
		try {
			cmbDisciplina.removeAllItems();
			if (alunoAtual == null)
				return;
			for (Disciplina d : disciplinaDAO.listarPorCurso(alunoAtual.getCursoId()))
				cmbDisciplina.addItem(d);
		} catch (SQLException ex) {
			msgErro("Erro ao carregar disciplinas:\n" + ex.getMessage());
		}
	}

	private void limparAbaNotas() {
		txtRgmNota.setText("");
		txtNomeNota.setText("");
		txtCursoNota.setText("");
		txtA1.setText("");
		txtA2.setText("");
		txtA3.setEnabled(false);
		txtA3.setText("");
		txtFaltasA1.setText("");
		txtFaltasA2.setText("");
		btnLancarA3.setEnabled(false);
		txtA1.setEditable(true);
		txtA2.setEditable(true);
		txtFaltasA1.setEditable(true);
		txtFaltasA2.setEditable(true);
		lblValorMedia.setText("0,0");
		lblStatusMedia.setText("Sem cálculo");
		lblTotalFaltas.setText("Total: 0  |  Limite: 20");
		lblTotalFaltas.setForeground(new Color(80, 80, 80));
		notaSelecionada = null;
		modeloBoletim.setRowCount(0);
		alunoAtual = null;
		cmbDisciplina.removeAllItems();
	}

	private void limparFormulario() {
		txtRgm.setText("");
		txtNome.setText("");
		txtDataNasc.setValue(null);
		txtCpf.setValue(null);
		txtEmail.setText("");
		txtEndereco.setText("");
		txtMunicipio.setText("");
		txtCelular.setValue(null);
		notaSelecionada = null;
		alunoAtual = null;
		btnLancarA3.setEnabled(false);
		cmbUf.setSelectedIndex(0);
		cmbCurso.setEnabled(true);
		if (cmbCurso.getItemCount() > 0)
			cmbCurso.setSelectedIndex(0);
		rbNoturno.setSelected(true);
		txtRgmNota.setText("");
		txtNomeNota.setText("");
		txtCursoNota.setText("");
		txtBoletimRgm.setText("");
		txtBoletimNome.setText("");
		txtBoletimCurso.setText("");
		txtA1.setText("");
		txtA2.setText("");
		txtA3.setText("");
		txtFaltasA1.setText("");
		txtFaltasA2.setText("");
		if (modeloBoletim != null)
			modeloBoletim.setRowCount(0);
	}

	// =========================================================
	// VALIDAÇÕES
	// =========================================================
	private boolean validarCamposAluno() {
		if (txtRgm.getText().trim().isEmpty()) {
			err(txtRgm, "Informe o RGM.");
			return false;
		}
		if (!txtRgm.getText().trim().matches("\\d+")) {
			err(txtRgm, "O RGM deve conter apenas números.");
			return false;
		}
		if (txtNome.getText().trim().isEmpty()) {
			err(txtNome, "Informe o nome.");
			return false;
		}
		if (!txtNome.getText().trim().matches("[A-Za-zÀ-ÿ\\s'-]+")) {
			err(txtNome, "O nome deve conter apenas letras.");
			return false;
		}
		if (txtDataNasc.getText().contains("_")) {
			err(txtDataNasc, "Preencha a data de nascimento corretamente.");
			return false;
		}
		if (txtCpf.getText().contains("_")) {
			err(txtCpf, "Preencha o CPF corretamente.");
			return false;
		}
		if (txtEmail.getText().trim().isEmpty()) {
			err(txtEmail, "Informe o e-mail.");
			return false;
		}
		if (!txtEmail.getText().trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
			err(txtEmail, "E-mail inválido.");
			return false;
		}
		if (txtEndereco.getText().trim().isEmpty()) {
			err(txtEndereco, "Informe o endereço.");
			return false;
		}
		if (txtMunicipio.getText().trim().isEmpty()) {
			err(txtMunicipio, "Informe o município.");
			return false;
		}
		if (txtCelular.getText().contains("_")) {
			err(txtCelular, "Preencha o número do celular corretamente.");
			return false;
		}
		if (cmbCurso.getSelectedItem() == null) {
			msgAviso("Selecione um curso na aba Curso.");
			return false;
		}

		limparCampoErro(txtRgm);
		limparCampoErro(txtNome);
		limparCampoErro(txtDataNasc);
		limparCampoErro(txtCpf);
		limparCampoErro(txtEmail);
		limparCampoErro(txtEndereco);
		limparCampoErro(txtMunicipio);
		limparCampoErro(txtCelular);
		return true;
	}

	private void err(JTextComponent c, String msg) {
		marcarCampoErro(c);
		msgErro(msg);
		c.requestFocus();
	}

	private boolean validarCamposNota() {
		if (cmbDisciplina.getSelectedItem() == null) {
			msgAviso("Selecione uma disciplina.");
			return false;
		}
		if (txtA1.getText().trim().isEmpty()) {
			msgAviso("Informe a nota A1.");
			txtA1.requestFocus();
			return false;
		}
		if (txtA2.getText().trim().isEmpty()) {
			msgAviso("Informe a nota A2.");
			txtA2.requestFocus();
			return false;
		}
		if (txtA3.isEnabled() && txtA3.getText().trim().isEmpty()) {
			msgAviso("Informe a nota A3.");
			txtA3.requestFocus();
			return false;
		}
		return true;
	}

	// =========================================================
	// MÉDIAS / A3
	// =========================================================
	private void atualizarMediaParcial() {
		try {
			if (txtA1.getText().trim().isEmpty() || txtA2.getText().trim().isEmpty()) {
				lblValorMedia.setText("0,0");
				lblStatusMedia.setText("Sem cálculo");
				return;
			}
			double a1 = Double.parseDouble(txtA1.getText().replace(",", "."));
			double a2 = Double.parseDouble(txtA2.getText().replace(",", "."));
			double a3 = txtA3.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtA3.getText().replace(",", "."));
			double media = calcularMedia(a1, a2, a3);
			lblValorMedia.setText(String.format("%.1f", media));
			lblStatusMedia.setText(media >= 6.0 ? "Aprovado" : "Precisa A3");
		} catch (Exception e) {
			lblValorMedia.setText("0,0");
			lblStatusMedia.setText("Sem cálculo");
		}
	}

	private double calcularMedia(double a1, double a2, double a3) {
		return a3 > 0 ? (Math.max(a1, a2) + a3) / 2.0 : (a1 + a2) / 2.0;
	}

	private void verificarA3() {
		try {
			double a1 = Double.parseDouble(txtA1.getText().replace(",", "."));
			double a2 = Double.parseDouble(txtA2.getText().replace(",", "."));
			double m = (a1 + a2) / 2.0;
			boolean precisaA3 = m < 6.0 && m > 0;
			txtA3.setEnabled(precisaA3);
			btnLancarA3.setEnabled(precisaA3);
			if (!precisaA3)
				txtA3.setText("");
		} catch (Exception e) {
			txtA3.setEnabled(false);
			btnLancarA3.setEnabled(false);
			txtA3.setText("");
		}

		try {
			double a1 = Double.parseDouble(txtA1.getText().replace(",", "."));
			double a2 = Double.parseDouble(txtA2.getText().replace(",", "."));
			double m = (a1 + a2) / 2.0;
			boolean precisaA3 = m < 6.0 && m > 0;
			txtA3.setEnabled(precisaA3);
			if (btnLancarA3 != null)
				btnLancarA3.setEnabled(precisaA3);
			if (!precisaA3)
				txtA3.setText("");
		} catch (Exception e) {
			txtA3.setEnabled(false);
			if (btnLancarA3 != null)
				btnLancarA3.setEnabled(false);
			txtA3.setText("");
		}
	}

	// =========================================================
	// HELPERS UI
	// =========================================================
	private JTextField campo(JPanel p, int x, int y, int w, int h, Font f) {
		JTextField t = new JTextField();
		t.setBounds(x, y, w, h);
		t.setBackground(COR_CAMPO);
		t.setFont(f);
		p.add(t);
		return t;
	}

	private void estilizarCampo(JTextComponent c, Font f) {
		c.setBackground(COR_CAMPO);
		c.setFont(f);
	}

	private JFormattedTextField fmt(JPanel p, String mask, int x, int y, int w, int h) {
		JFormattedTextField f;
		try {
			MaskFormatter mf = new MaskFormatter(mask);
			mf.setPlaceholderCharacter('_');
			f = new JFormattedTextField(mf);
		} catch (ParseException ex) {
			f = new JFormattedTextField();
		}
		f.setBounds(x, y, w, h);
		p.add(f);
		return f;
	}

	private void lbl(JPanel p, String txt, int x, int y, int w, int h) {
		JLabel l = new JLabel(txt);
		l.setBounds(x, y, w, h);
		p.add(l);
	}

	private JButton btn(String texto, int x, int y, int w, int h) {
		JButton b = new JButton(texto);
		b.setBounds(x, y, w, h);
		b.setFont(new Font("Segoe UI", Font.BOLD, 12));
		b.setFocusPainted(false);
		b.setBackground(Color.WHITE);
		b.setForeground(Color.BLACK);
		b.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
		return b;
	}

	private JPanel card(JPanel parent, int x, int y, int w, int h) {
		JPanel c = new JPanel(null);
		c.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
		c.setBackground(new Color(240, 240, 235));
		c.setBounds(x, y, w, h);
		parent.add(c);
		return c;
	}

	private JRadioButton rb(JPanel p, String texto, int x, int y, int w, int h, Font f) {
		JRadioButton r = new JRadioButton(texto);
		r.setBounds(x, y, w, h);
		r.setBackground(COR_CAMPO);
		r.setFont(f);
		r.setEnabled(false);
		p.add(r);
		return r;
	}

	private void marcarCampoErro(JTextComponent c) {
		c.setBackground(new Color(255, 200, 200));
	}

	private void limparCampoErro(JTextComponent c) {
		c.setBackground(COR_CAMPO);
	}

	private void permitirNotaValida(JTextField campo) {
		campo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (c == KeyEvent.VK_BACK_SPACE)
					return;
				String atual = campo.getText();
				if (Character.isDigit(c)) {
					try {
						double val = Double.parseDouble((atual + c).replace(",", "."));
						if (val > 10) {
							e.consume();
							return;
						}
					} catch (NumberFormatException ignored) {
					}
					return;
				}
				if ((c == '.' || c == ',') && !atual.contains(".") && !atual.contains(","))
					return;
				e.consume();
			}
		});
		campo.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String txt = campo.getText().trim();
				if (txt.isEmpty())
					return;
				try {
					double val = Double.parseDouble(txt.replace(",", "."));
					if (val < 0 || val > 10) {
						msgErro("A nota deve ser entre 0 e 10.");
						campo.setText("");
						campo.requestFocus();
					}
				} catch (NumberFormatException ex) {
					campo.setText("");
				}
			}
		});
	}

	private void permitirSomenteLetras(JTextField campo) {
		campo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (Character.isLetter(c) || Character.isWhitespace(c) || c == KeyEvent.VK_BACK_SPACE || c == '\''
						|| c == '-')
					return;
				e.consume();
			}
		});
	}

	private void permitirSomenteInteiros(JTextField campo) {
		campo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (campo.getText().length() >= 10) {
					e.consume();
					return;
				}
				if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE)
					e.consume();
			}
		});
	}
	
	
	private void mostrarSobre() {
	    JDialog dlg = new JDialog(this, "Ajuda do Sistema", true);
	    dlg.setSize(620, 580);
	    dlg.setResizable(false);
	    dlg.setLocationRelativeTo(this);
	    dlg.setLayout(new BorderLayout());

	    Color azulPrincipal = new Color(33, 87, 153);
	    Color azulEscuro = new Color(25, 60, 110);
	    Color azulClaro = new Color(240, 244, 250);

	    // =========================
	    // CABEÇALHO
	    // =========================
	    JPanel header = new JPanel(null);
	    header.setPreferredSize(new Dimension(620, 75));
	    header.setBackground(azulPrincipal);

	    JLabel lblTitulo = new JLabel("Central de Ajuda do Sistema");
	    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
	    lblTitulo.setForeground(Color.WHITE);
	    lblTitulo.setBounds(22, 12, 500, 28);
	    header.add(lblTitulo);

	    JLabel lblSub = new JLabel("Manual rápido de utilização das funcionalidades");
	    lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
	    lblSub.setForeground(new Color(230, 230, 230));
	    lblSub.setBounds(24, 42, 400, 18);
	    header.add(lblSub);

	    dlg.add(header, BorderLayout.NORTH);

	    // =========================
	    // CONTEÚDO
	    // =========================
	    JPanel conteudo = new JPanel();
	    conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
	    conteudo.setBackground(Color.WHITE);
	    conteudo.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

	    adicionarSecaoAjuda(conteudo,
	            "Dados Pessoais",
	            "Área responsável pelo cadastro e gerenciamento dos alunos.",
	            new String[]{
	                    "Salvar: preencha todos os campos obrigatórios e clique em Salvar para cadastrar um novo aluno no sistema.",
	                    "Consultar: informe o RGM do aluno e clique em Consultar para carregar os dados.",
	                    "Alterar: após consultar, edite os campos desejados e clique em Alterar.",
	                    "Excluir: remove o cadastro do aluno após confirmação.",
	                    "Limpar: limpa todos os campos da tela.",
	                    "Sair: encerra a aplicação."
	            });

	    adicionarSecaoAjuda(conteudo,
	            "Curso",
	            "Permite vincular um curso ao aluno cadastrado.",
	            new String[]{
	                    "Primeiro consulte um aluno na aba Dados Pessoais.",
	                    "Selecione o curso desejado.",
	                    "O campo Campus será preenchido automaticamente.",
	                    "Escolha o período (Matutino, Vespertino ou Noturno).",
	                    "Clique em Salvar para concluir o vínculo do curso."
	            });

	    adicionarSecaoAjuda(conteudo,
	            "Notas e Faltas",
	            "Área utilizada para registrar avaliações e frequência.",
	            new String[]{
	                    "Informe o RGM do aluno.",
	                    "Selecione o semestre e a disciplina.",
	                    "Clique em Consultar para carregar os dados.",
	                    "Salvar A1 / A2: registra cada avaliação separadamente.",
	                    "Lançar A3: disponível apenas em recuperação.",
	                    "Alterar: atualiza notas ou faltas.",
	                    "Excluir: remove o lançamento da disciplina.",
	                    "A média e o status são calculados automaticamente."
	            });

	    adicionarSecaoAjuda(conteudo,
	            "Boletim",
	            "Exibe e exporta o boletim completo do aluno.",
	            new String[]{
	                    "Informe o RGM e clique em Consultar.",
	                    "O sistema exibirá todas as disciplinas, notas, faltas e situação.",
	                    "Clique em Exportar PDF para gerar o boletim em PDF.",
	                    "O arquivo será salvo automaticamente na pasta do projeto."
	            });

	    adicionarSecaoAjuda(conteudo,
	            "Dicas Importantes",
	            "Boas práticas para utilização do sistema.",
	            new String[]{
	                    "Preencha corretamente todos os campos obrigatórios.",
	                    "Sempre consulte um aluno antes de alterar ou excluir.",
	                    "Utilize o botão Limpar antes de iniciar um novo cadastro.",
	                    "Evite inserir letras em campos numéricos."
	            });

	    JScrollPane scroll = new JScrollPane(conteudo);
	    scroll.setBorder(null);
	    scroll.getVerticalScrollBar().setUnitIncrement(14);
	    dlg.add(scroll, BorderLayout.CENTER);

	    // =========================
	    // RODAPÉ
	    // =========================
	    JPanel rodape = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
	    rodape.setBackground(azulClaro);
	    rodape.setBorder(BorderFactory.createMatteBorder(
	            1, 0, 0, 0,
	            new Color(190, 210, 230)));

	    JButton btnFechar = new JButton("Fechar");
	    btnFechar.setFont(new Font("Segoe UI", Font.BOLD, 16));
	    btnFechar.setPreferredSize(new Dimension(150, 40));
	    Color azulTopo = new Color(33, 87, 153);
	    btnFechar.setBackground(azulTopo);
	    btnFechar.setForeground(Color.WHITE);
	    btnFechar.setFocusPainted(false);
	    btnFechar.setOpaque(true);
	    btnFechar.setBorderPainted(false);
	    btnFechar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    btnFechar.setBorder(BorderFactory.createEmptyBorder());
	    btnFechar.setOpaque(true);

	    btnFechar.addMouseListener(new java.awt.event.MouseAdapter() {
	        public void mouseEntered(java.awt.event.MouseEvent evt) {
	            btnFechar.setBackground(new Color(18, 45, 85));
	        }

	        public void mouseExited(java.awt.event.MouseEvent evt) {
	            btnFechar.setBackground(azulEscuro);
	        }
	    });

	    btnFechar.addActionListener(e -> dlg.dispose());

	    rodape.add(btnFechar);
	    dlg.add(rodape, BorderLayout.SOUTH);

	    dlg.setVisible(true);
	}

	private void adicionarSecaoAjuda(
	        JPanel painel,
	        String titulo,
	        String descricao,
	        String[] itens) {

	    Color azulTitulo = new Color(33, 87, 153);

	    JPanel card = new JPanel();
	    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
	    card.setBackground(new Color(248, 250, 252));

	    card.setBorder(BorderFactory.createCompoundBorder(
	            BorderFactory.createLineBorder(new Color(210, 225, 240)),
	            BorderFactory.createEmptyBorder(12, 14, 12, 14)
	    ));

	    card.setAlignmentX(Component.LEFT_ALIGNMENT);
	    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

	    JLabel lblTitulo = new JLabel(titulo);
	    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
	    lblTitulo.setForeground(azulTitulo);
	    lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

	    JLabel lblDescricao = new JLabel(
	            "<html><body style='width:500px'>" + descricao + "</body></html>");

	    lblDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
	    lblDescricao.setForeground(new Color(90, 90, 90));
	    lblDescricao.setBorder(BorderFactory.createEmptyBorder(3, 0, 10, 0));
	    lblDescricao.setAlignmentX(Component.LEFT_ALIGNMENT);

	    card.add(lblTitulo);
	    card.add(lblDescricao);

	    for (String item : itens) {

	        JLabel lblItem = new JLabel(
	                "<html><body style='width:500px'>• " + item + "</body></html>");

	        lblItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
	        lblItem.setForeground(new Color(45, 45, 45));
	        lblItem.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 0));
	        lblItem.setAlignmentX(Component.LEFT_ALIGNMENT);

	        card.add(lblItem);
	    }

	    painel.add(card);
	    painel.add(Box.createVerticalStrut(12));
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new TelaPrincipal().setVisible(true));
	}

}