package org.inria.websmatch.gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.inria.websmatch.gui.renderers.AttributeCellRenderer;
import org.inria.websmatch.models.ShowTableModel;
import org.inria.websmatch.utils.L;
//import org.mitre.schemastore.porters.ImporterException;
//import org.mitre.schemastore.porters.schemaImporters.SpreadsheetImporter;

public class ShowTable {

	private JFrame frmXlsImporter;
	private JTable table;

	private static boolean _DEBUG = true;
	private JScrollPane scrollPane;
	private JTabbedPane tabbedPane;
	private JTextPane outputTextPane;

	private boolean firstTime = true;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ShowTable window = new ShowTable();
					window.frmXlsImporter.setVisible(true);
				} catch (Exception e) {
					L.Error(e.getMessage(),e);
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ShowTable() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
/*
		frmXlsImporter = new JFrame();
		frmXlsImporter.setTitle("XLS Importer");
		frmXlsImporter.setName("XLS Importer");
		frmXlsImporter.setBounds(100, 100, 684, 593);
		frmXlsImporter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JButton btnOpenSpreadsheet = new JButton("Open spreadsheet");
		btnOpenSpreadsheet.setToolTipText("Open and visualize a spreadsheet");
		btnOpenSpreadsheet.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"XLS spreadsheets", "xls");
				chooser.setFileFilter(filter);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int returnVal = chooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					if (_DEBUG) {
						System.out.println("You chose to open this file: "
								+ chooser.getSelectedFile().getName());
					}

					File target = new File(chooser.getSelectedFile()
							.getAbsolutePath());

					File[] files;

					if (target.isFile()) {
						files = new File[1];
						files[0] = new File(target.getAbsolutePath());
					}

					else
						files = target.listFiles();

					for (int i = 0; i < files.length; i++) {

						// if the file is a spreadsheet file
						if (files[i].isFile() && filter.accept(files[i])) {

							// so we make the table
							SpreadsheetImporter xlsImporter = new SpreadsheetImporter();
							try {

								if (_DEBUG) {
									System.out.println("File loading : "
											+ files[i]);
								}

								xlsImporter.getSchemaElements(new URI("file:"
										+ files[i]));

								// for each sheet
								for (int sn = 0; sn < xlsImporter
										.getAttribCells().size(); sn++) {
									ShowTableModel stm = new ShowTableModel(
											xlsImporter.getSheetNames().get(sn),
											xlsImporter.getDatas().get(sn),
											xlsImporter.getAttribCells()
													.get(sn));

									if (firstTime) {
										((JTable) ((JViewport) ((JScrollPane) tabbedPane
												.getSelectedComponent())
												.getComponent(0))
												.getComponent(0)).setModel(stm);
										firstTime = false;
									} else {

										JTable tmpTable = new JTable();

										JScrollPane tmpScrollPane = new JScrollPane();
										tmpScrollPane
												.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
										tmpScrollPane
												.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
										tabbedPane.addTab("Entity", null,
												tmpScrollPane, null);

										tmpTable.setAutoCreateRowSorter(true);
										tmpScrollPane.setViewportView(tmpTable);

										tabbedPane
												.setSelectedComponent(tmpScrollPane);

										// we set to the renderer wich
										// colors
										// attributes
										tmpTable.setDefaultRenderer(
												Object.class,
												new AttributeCellRenderer());

										tmpTable.setModel(stm);

									}

									tabbedPane.setTitleAt(
											tabbedPane.getSelectedIndex(),
											stm.getTitle());

								}

								outputTextPane.setText(outputTextPane.getText()
										+ "Loaded file : " + files[i] + "\n");

								try {
									outputTextPane
											.scrollRectToVisible(outputTextPane
													.modelToView(outputTextPane
															.getDocument()
															.getLength()));
								} catch (javax.swing.text.BadLocationException err) {

								}

							} catch (ImporterException e) {
								// TODO Auto-generated catch block
								L.Error(e.getMessage(),e);
							} catch (URISyntaxException e) {
								// TODO Auto-generated catch block
								L.Error(e.getMessage(),e);
							}

						}

					}

				}

			}
		});

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);

		scrollPane = new JScrollPane();
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedPane.addTab("Entity", null, scrollPane, null);

		table = new JTable();
		table.setAutoCreateRowSorter(true);
		scrollPane.setViewportView(table);

		// we set to the renderer wich colors attributes
		table.setDefaultRenderer(Object.class, new AttributeCellRenderer());

		JScrollPane txtscrollPane = new JScrollPane();
		txtscrollPane.setBorder(new TitledBorder(null, "Output",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		txtscrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		txtscrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		outputTextPane = new JTextPane();

		txtscrollPane.setViewportView(outputTextPane);

		outputTextPane.setEditable(false);
		GroupLayout groupLayout = new GroupLayout(
				frmXlsImporter.getContentPane());
		groupLayout
				.setHorizontalGroup(groupLayout
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								groupLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.TRAILING)
														.addComponent(
																txtscrollPane,
																Alignment.LEADING,
																GroupLayout.DEFAULT_SIZE,
																658,
																Short.MAX_VALUE)
														.addGroup(
																Alignment.LEADING,
																groupLayout
																		.createSequentialGroup()
																		.addComponent(
																				btnOpenSpreadsheet)
																		.addGap(22)
																		.addComponent(
																				tabbedPane)))
										.addGap(8)));
		groupLayout
				.setVerticalGroup(groupLayout
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								groupLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.LEADING)
														.addComponent(
																btnOpenSpreadsheet)
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addGap(1)
																		.addComponent(
																				tabbedPane,
																				GroupLayout.DEFAULT_SIZE,
																				312,
																				Short.MAX_VALUE)))
										.addPreferredGap(
												ComponentPlacement.UNRELATED)
										.addComponent(txtscrollPane,
												GroupLayout.PREFERRED_SIZE,
												212, GroupLayout.PREFERRED_SIZE)
										.addContainerGap()));
		frmXlsImporter.getContentPane().setLayout(groupLayout);
*/
	}
}
