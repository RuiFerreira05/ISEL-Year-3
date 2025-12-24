package gestor;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;

import javax.imageio.*; // Para ler e escrever formatos de imagem (ex: JPG, PNG)
import javax.swing.*; // Para interface gráfica (exibir a imagem)

import util.Configura;

/**
 * Classe utilitária para manipulação de fotografias, armazenando-as como um array de bytes (BLOB).
 * Suporta carregamento de ficheiros, serialização Base64, e comparação de imagens.
 * @author Engº Porfírio Filipe
 */
public class Foto {
	// Array de bytes que armazena a imagem. É a representação binária da foto (BLOB).
	byte[] img = null;

	// Nome do ficheiro de imagem padrão (silhueta) a ser usado quando a foto estiver omissa.
	public final static String omissa = "silhueta.jpg";
	
	// Caminho absoluto (real) do servidor ou diretório de trabalho, obtido via Configura.
	public static String path = new Configura().getRealPath();

	/**
	 * Método principal para demonstração e teste da classe Foto.
	 * @param args Argumentos de linha de comandos (não utilizados).
	 * @throws Exception Em caso de erros de I/O ou outros.
	 */
	public static void main(String[] args) throws Exception { 
		Foto ft1 = new Foto();
		Foto ft2 = new Foto();
		String f1="silhueta.jpg";
		String f2="silhueta.png";
		
		// ℹ️ Define o caminho real (assumindo que Configura está a funcionar)
		Foto.setPath(new Configura().getRealPath()); 
		
		// 1. Carrega a primeira imagem
		ft1.load(f1);
		ft1.show("Fotografia: "+f1);
		
		// 2. Carrega a segunda imagem
		ft2.load(f2);
		ft2.show("Fotografia: "+f2);
		
		// 3. Compara as imagens e apresenta o resultado
		System.out.println("🔬 Semelhança entre as fotos: "+ft1.compareTo(ft2)+"%");
	}
	
	/**
	 * Altera o caminho base onde os ficheiros de imagem são procurados.
	 * @param path O novo caminho de diretório absoluto.
	 */
	public static void setPath(String path) {
		Foto.path=path;
	}
	
	/**
	 * Altera o array de bytes armazenado (o conteúdo da imagem).
	 * @param img O novo array de bytes da imagem.
	 */
	public void setFoto(byte[] img) {
		this.img=img;
	}
	
	/**
	 * Devolve o array de bytes armazenado que representa a imagem.
	 * @return O array de bytes da foto (BLOB).
	 */
	public byte[] getFoto() {
		return img;
	}
	
	/**
	 * Descodifica uma string Base64 e define o array de bytes armazenado.
	 * @param encoded64 A string da imagem codificada em Base64.
	 */
	public void setFoto64(String encoded64) {
		// Base64.getDecoder().decode(encoded64); // CORREÇÃO: falta atribuir o resultado.
		this.img = Base64.getDecoder().decode(encoded64);
		System.out.println("✅ Imagem descodificada a partir de Base64.");
	}
	
	/**
	 * Devolve o array de bytes da imagem codificado como uma string Base64.
	 * @return A string Base64 da imagem.
	 */
	public String getFoto64() {
		return Base64.getEncoder().encodeToString(img);
	}
	
	/**
	 * Método auxiliar estático para converter um array de bytes (`byte[]`) numa `BufferedImage`.
	 * @param imageData O array de bytes da imagem.
	 * @return O objeto BufferedImage (representação da imagem na memória Java).
	 */
	private static BufferedImage createImageFromBytes(byte[] imageData) {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData);) {
			// ImageIO.read tenta detetar e ler o formato da imagem (JPG, PNG, etc.)
			return ImageIO.read(bais);
		} catch (IOException e) {
			// Propaga o erro como uma RuntimeException para simplificar o tratamento
			System.err.println("❌ Erro de I/O na criação da BufferedImage: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Devolve a representação da imagem na memória Java (BufferedImage) a partir dos bytes armazenados.
	 * @return O objeto BufferedImage.
	 */
	public BufferedImage getBufferedImage() {
		return Foto.createImageFromBytes(img);
	}
	
	/**
	 * Apresenta a imagem numa janela Swing com o título padrão "Fotografia".
	 */
	public void show() {
		show("Fotografia");
	}
	
	/**
	 * Mostra a imagem contida no array de bytes (`img`) numa nova janela Swing.
	 * @param titulo O título da janela a ser apresentada.
	 */
	public void show(String titulo) {
		// O SwingUtilities.invokeLater é crucial para a segurança de threads da GUI Swing
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame editorFrame = new JFrame(titulo);
				// A linha abaixo é comentada para não fechar a aplicação inteira
				// editorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); 
				BufferedImage image = null;
				
				// Se o array de bytes estiver nulo, tenta carregar a imagem omissa (silhueta)
				if (img == null) {
					System.out.println("ℹ️ Foto omissa. A carregar imagem padrão: " + omissa);
					try {
						load(omissa);
					} catch (Exception e) {
						System.err.println("❌ Erro: Não foi possível carregar a imagem omissa.");
						return; // Sai se não conseguir carregar a imagem omissa
					}
				}
				
				image = getBufferedImage();
				ImageIcon imageIcon = new ImageIcon(image);
				JLabel jLabel = new JLabel();
				jLabel.setIcon(imageIcon);
				editorFrame.getContentPane().add(jLabel, BorderLayout.CENTER);
				editorFrame.pack(); // Ajusta o tamanho da janela ao conteúdo
				editorFrame.setLocationRelativeTo(null); // Centraliza a janela
				editorFrame.setVisible(true);
				System.out.println("🖼️ Janela de visualização aberta: " + titulo);
			}
		});
	}
	
	/**
	 * Carrega o conteúdo de um ficheiro de imagem para o array de bytes interno (`img`).
	 * @param filename O nome do ficheiro (assumindo que está no 'path').
	 * @throws Exception Em caso de File Not Found ou erro de I/O.
	 */
	public void load(final String filename) throws Exception {
		// Constrói o caminho completo: caminho base + nome do ficheiro
		File file = new File(path + filename);
		
		// Usa try-with-resources para garantir o fecho do FileInputStream
		try (FileInputStream fl = new FileInputStream(file)) {
			// Cria um novo array de bytes com o tamanho exato do ficheiro
			img = new byte[(int) file.length()];
			// Lê todo o conteúdo do ficheiro para o array de bytes
			fl.read(img);
			System.out.println("📥 Ficheiro de imagem carregado com sucesso: " + filename);
		} catch (FileNotFoundException fnf) {
			System.err.println("⚠️ Ficheiro de imagem não encontrado: " + path + filename);
			throw fnf;
		} catch (IOException ioe) {
			System.err.println("❌ Erro de I/O ao carregar o ficheiro: " + ioe.getMessage());
			throw ioe;
		}
	}
	
	/**
	 * Escreve o array de bytes interno (`img`) para um ficheiro com o nome indicado.
	 * @param filename O nome do ficheiro de destino.
	 * @throws Exception Em caso de erro de I/O.
	 */
	public void save(final String filename) throws Exception {
		// Usa try-with-resources para garantir o fecho do FileOutputStream
		try (FileOutputStream fos = new FileOutputStream(path + filename)) {
			fos.write(img);
			System.out.println("💾 Imagem guardada com sucesso no ficheiro: " + filename);
		} catch (IOException ioe) {
			System.err.println("❌ Erro de I/O ao guardar o ficheiro: " + ioe.getMessage());
			throw ioe;
		}
	}
	
	/**
	 * Compara a imagem armazenada (this) com outra imagem (ft) e devolve a percentagem de semelhança.
	 * O algoritmo compara o valor RGB de cada pixel (cor) de ambas as imagens.
	 * @param ft A outra fotografia (objeto Foto) a comparar.
	 * @return A percentagem de semelhança (BigDecimal com 2 casas decimais), ou -1 em caso de erro.
	 * @throws Exception Em caso de erro ao obter a BufferedImage.
	 */
	public BigDecimal compareTo(Foto ft) throws Exception {
		BufferedImage imgA = getBufferedImage();
		BufferedImage imgB = ft.getBufferedImage();
		
		int width1 = imgA.getWidth();
		int width2 = imgB.getWidth();
		int height1 = imgA.getHeight();
		int height2 = imgB.getHeight();
		
		// 1. Verificação de dimensões
		if ((width1 != width2) || (height1 != height2)) {
			// Apenas imagens com o mesmo tamanho podem ser comparadas pixel a pixel.
			System.err.println("❌ Erro de Comparação: As dimensões das imagens não coincidem.");
			return new BigDecimal(-1);
		}
		
		long difference = 0;
		// 2. Iteração sobre os pixels
		for (int y = 0; y < height1; y++) {
			for (int x = 0; x < width1; x++) {
				int rgbA = imgA.getRGB(x, y);
				int rgbB = imgB.getRGB(x, y);
				
				// Extrai as componentes RGB da Imagem A
				int redA = (rgbA >> 16) & 0xff;
				int greenA = (rgbA >> 8) & 0xff;
				int blueA = (rgbA) & 0xff;
				
				// Extrai as componentes RGB da Imagem B
				int redB = (rgbB >> 16) & 0xff;
				int greenB = (rgbB >> 8) & 0xff;
				int blueB = (rgbB) & 0xff;
				
				// Soma a diferença absoluta entre as componentes (R, G, B)
				difference += Math.abs(redA - redB);
				difference += Math.abs(greenA - greenB);
				difference += Math.abs(blueA - blueB);
			}
		}
		
		// 3. Cálculo da Percentagem
		// Total de comparações = (Largura * Altura) * 3 (R, G, B)
		double total_pixels = width1 * height1 * 3.0; 
		
		// Média da diferença de pixel por componente (normalização)
		double avg_different_pixels = difference / total_pixels; 
		
		// Converte a diferença média (intervalo [0, 255]) para uma percentagem de diferença [0, 100]
		double percentageDiff = (avg_different_pixels / 255.0) * 100.0;
		
		// Percentagem de Semelhança = 100 - Percentagem de Diferença
		double percentageSimilarity = 100.0 - percentageDiff;
		
		BigDecimal bd = BigDecimal.valueOf(percentageSimilarity);
		
		// 4. Devolve o resultado arredondado a duas casas decimais.
		System.out.println("📊 Comparação concluída.");
		return bd.setScale(2, RoundingMode.HALF_UP);
	}
}