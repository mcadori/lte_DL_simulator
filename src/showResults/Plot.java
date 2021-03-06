package showResults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lteStructure.Packet;
import lteStructure.SB;
import lteStructure.TTI;
import scheduling.Constants;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.NamedPlotColor;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;

public class Plot {

	/**
	 * Plota o bit rate de cada SB pelo n total de SB.
	 * 
	 * @param ttiTmp
	 */
	public static void plotSBxRate(ArrayList<TTI> ttiTmp) {

		ArrayList<TTI> dataTmp = ttiTmp;
		ArrayList<Double> aux = getSBThroughput(dataTmp);

		//double meanSBsRate = calcMeanRate(aux);
		//double meanSBsRate = calcMeanRateSample(aux); // Retorna media de aux/10
		
		double[][] dataToPlot = new double[aux.size()][2];

		for (int row = 0; row < aux.size(); row++) {
			for (int col = 0; col < dataToPlot[row].length; col++) {
				dataToPlot[row][0] = row;
				dataToPlot[row][1] = aux.get(row);
			}
		}
		
		JavaPlot p = new JavaPlot();
		DataSetPlot s = new DataSetPlot(dataToPlot);
		s.setTitle("Mbps");
		p.setTitle("An�lise de Rate x SBs");
		p.getAxis("x").setLabel("SB");
		p.getAxis("y").setLabel("Rate (Mbps)");
		p.getAxis("x").setBoundaries(0.0, aux.size());
		p.getAxis("y").setBoundaries(0.0, 1.1);

		PlotStyle myPlotStyle = new PlotStyle();
		// myPlotStyle.setStyle(Style.POINTS);
		myPlotStyle.setStyle(Style.LINESPOINTS);
		myPlotStyle.setPointSize(1);
		myPlotStyle.setPointType(6);
		s.setPlotStyle(myPlotStyle);

		p.addPlot(s);
		p.newGraph();
		
		/* Para incluir um grafico do rate medio do escalonamento descomentar esta parte */
//		double[][] r = { { 1, meanSBsRate } };
//		DataSetPlot w = new DataSetPlot(r);
//		w.setTitle("Rate absoluto");
//		p.getAxis("x").setLabel("");
//		p.getAxis("y").setLabel("Rate m�dio");
//		p.getAxis("x").setBoundaries(0,2);
//		p.getAxis("y").setBoundaries(0, 1);
//		p.addPlot(w);
		
		p.plot();
	}

	/**
	 * Recebe um array com rates de SBs e retorna uma media, i.e. <br>
	 * soma_rate_todos_SBs/numero_SB
	 * @param aux
	 * @return
	 */
	public static double calcMeanRate(ArrayList<Double> aux) {
		ArrayList<Double> totalSB = aux;
		double sumSB = 0;
		for (Double atualSB : totalSB)
			sumSB = atualSB + sumSB;
		return sumSB / totalSB.size();
	}

	/**
	 * Recebe os dados da simulacao obtem o throughput de cada SB da simula��o como um todo <br>
	 * levando em conta todos os TTIs, ent�o, retorna um array com os throughputs normalizados <br>
	 * i.e. rate/rateMax.
	 * 
	 * @param ttiTmp
	 * @return
	 */
	public static ArrayList<Double> getSBThroughput(ArrayList<TTI> ttiTmp) {
		ArrayList<TTI> itr = ttiTmp;
		ArrayList<SB> itr1 = null;
		ArrayList<Double> aux = new ArrayList<Double>();
		double maxThroughput=1.008;

		for (int i = 0; i < itr.size(); i++) {
			TTI element = itr.get(i);
			itr1 = element.getSchedulingBlocks();
			for (int j = 0; j < itr1.size(); j++) {
				SB element1 = itr1.get(j);
				aux.add(element1.getThroughput()/maxThroughput);
			}
		}
		return aux;
	}

	/**
	 * Plota o pkt no tempo (delay) que ele foi transmitido i.e.  <br>
	 * se tivermos um ponto (x=0,y=50) significa que qndo o pkt foi <br> 
	 * transmitido o tempo (delay) do pkt era 50 ms.
	 * 
	 * @param ttiTmp
	 */
	public static void plotSBxDelay(ArrayList<TTI> ttiTmp) {

		ArrayList<TTI> dataTmp = ttiTmp;
		ArrayList<Double> aux = getSBDelay(dataTmp);
		
		double[][] dataToPlot = new double[aux.size()][2];

		for (int row = 0; row < aux.size(); row++) {
			for (int col = 0; col < dataToPlot[row].length; col++) {
				dataToPlot[row][0] = aux.get(row);
				dataToPlot[row][1] = row;
			}
		}
		
		// Para limitar o tamanho dos limites do gr�fico, pega maior e menor valor de aux
		 Object max_obj = Collections.max(aux);
		 Object min_obj = Collections.min(aux);
		 Double min = (Double) min_obj;
		 Double max = (Double) max_obj;

		JavaPlot p = new JavaPlot();
		DataSetPlot s = new DataSetPlot(dataToPlot);
		s.setTitle("Pacotes");
		p.setTitle("An�lise do atraso no escalonamento");
		p.getAxis("x").setLabel("Linha do tempo (ms)");
		p.getAxis("y").setLabel("ID do SB onde esta o pacote");
		p.getAxis("x").setBoundaries(min-20, max+20);
		p.getAxis("y").setBoundaries(0.0, aux.size());

		PlotStyle myPlotStyle = new PlotStyle();
		myPlotStyle.setStyle(Style.POINTS);
		myPlotStyle.setLineType(NamedPlotColor.BLUE);
		myPlotStyle.setPointType(6);
		s.setPlotStyle(myPlotStyle);

		p.addPlot(s);
		p.newGraph();
		p.plot();
	}
	
	/**
	 *  Plota dados de fairness.
	 *  
	 * @param simulation
	 */
	public static void plotFairness(ArrayList<TTI> simulation) {
		
		Double[] aux1 = new Double[Constants.getN_USERS()+1];		
		aux1 = getFairnessPerUser(simulation);		
		Double fairness = getFairnessIndex(simulation);
		double[][] dataToPlot = new double[Constants.getN_USERS()+1][2];
		
		double max=0;
		for (Double tmp : aux1) {
			if (tmp>max)
				max=tmp;
		}
		
		int x = 0;
		for (Double temp : aux1) {
			dataToPlot[x][0] = x;
			dataToPlot[x][1] = temp;
			x++;
		}
		
		JavaPlot fairnessPlot = new JavaPlot();
		DataSetPlot s = new DataSetPlot(dataToPlot);
		s.setTitle("Rate por usu�rio");
		
		fairnessPlot.setTitle("Resultados Parciais");
		fairnessPlot.setTitle("An�lise de justi�a");
		fairnessPlot.getAxis("y").setLabel("Rate (Mbps)");
		fairnessPlot.getAxis("x").setLabel("Usuario");
		fairnessPlot.getAxis("y").setBoundaries(0, max+10);
		fairnessPlot.getAxis("x").setBoundaries(-1, Constants.getN_USERS()+1);

		PlotStyle myPlotStyle = new PlotStyle();
		
		myPlotStyle.setStyle(Style.IMPULSES);
		myPlotStyle.setLineType(NamedPlotColor.BLUE);
		myPlotStyle.setLineWidth(2);
		s.setPlotStyle(myPlotStyle);

		fairnessPlot.addPlot(s);		
		fairnessPlot.newGraph();
		
		fairnessPlot.getAxis("y").setLabel("Indice de fairness");
		fairnessPlot.getAxis("x").setLabel("");
		fairnessPlot.getAxis("y").setBoundaries(0, 1);
		fairnessPlot.getAxis("x").setBoundaries(0, 2);
		
		double[][] r = { { 1, fairness } };

		fairnessPlot.addPlot(r);
		fairnessPlot.newGraph();
		fairnessPlot.plot();
	}

	/**
	 * Recebe uma simula��o e retorna um double que indica o fairness calculado <br>
	 * atrav�s da equa��o do jain. 
	 * 
	 * fairness=(Math.pow(Math.abs(sum_rate),2) / (Constants.N_USERS*sum_quadrada));
	 * 
	 * @param simulation
	 * @return
	 */
	public static Double getFairnessIndex(ArrayList<TTI> simulation) {
		ArrayList<TTI> itr = simulation;
		ArrayList<SB> itr1 = null;
		Double[] aux = new Double[Constants.getN_USERS()+1];
		
		for(int p=0;p<=Constants.getN_USERS();p++)
			aux[p]=0.0;
		
		for (int i = 0; i < itr.size(); i++) {
			TTI element = itr.get(i);
			itr1 = element.getSchedulingBlocks();

			for (int j = 0; j < itr1.size(); j++) {
				SB element1 = itr1.get(j);
				
				for (int k=1;k<=Constants.getN_USERS();k++){
					if (element1.getPacket().getUser() == k){
						aux[k]= aux[k] + element1.getThroughput();
					}  
				} 
			}
		}
		
		Double sum_rate = 0., sum_quadrada = 0., fairness=0.;

		for(int p=1;p<=Constants.getN_USERS();p++){
			//System.out.println("Rate do user "+p+" = "+aux[p]);
			sum_rate=(aux[p]/Constants.getN_USERS())+sum_rate;
			sum_quadrada=Math.pow((aux[p]/Constants.getN_USERS()),2)+sum_quadrada;
		}
				
		fairness=(Math.pow(Math.abs(sum_rate),2) / (Constants.getN_USERS()*sum_quadrada));
		return fairness;
	}
	
	/**
	 * Recebe uma simula��o e retorna um array de double com a soma dos rates de cada usuario
	 * 
	 * @param simulation
	 * @return
	 */
	public static Double[] getFairnessPerUser(ArrayList<TTI> simulation){
		ArrayList<TTI> itr = simulation;
		ArrayList<SB> itr1 = null;
		Double[] aux = new Double[Constants.getN_USERS()+1];
		
		for(int p=0;p<=Constants.getN_USERS();p++)
			aux[p]=0.0;
		
		for (int i = 0; i < itr.size(); i++) {
			TTI element = itr.get(i);
			itr1 = element.getSchedulingBlocks();

			for (int j = 0; j < itr1.size(); j++) {
				SB element1 = itr1.get(j);
				
				for (int k=1;k<=Constants.getN_USERS();k++){
					if (element1.getPacket().getUser() == k){
						aux[k]= aux[k] + element1.getThroughput();
					}  
				} 
			}
		}
		return aux;
	}

	/**
	 * Recebe os dados da simulacao e obtem o delay de cada Pacote e retorna um array com
	 * os delays
	 * 
	 * @param ttiTmp
	 * @return
	 */
	public static ArrayList<Double> getSBDelay(ArrayList<TTI> ttiTmp) {
		ArrayList<TTI> itr = ttiTmp;
		ArrayList<SB> itr1 = null;
		ArrayList<Double> aux = new ArrayList<Double>();

		for (int i = 0; i < itr.size(); i++) {
			TTI element = itr.get(i);
			itr1 = element.getSchedulingBlocks();
			for (int j = 0; j < itr1.size(); j++) {
				SB element1 = itr1.get(j);
				aux.add((double) element1.getPacket().getDelay());
			}
		}
		return aux;
	}

	/**
	 * Recebe uma simula��o e retorna um array com o user que esta usando cada SB
	 * 
	 * @param ttiTmp
	 * @return
	 */
	public static List<Double> getSBUser(ArrayList<TTI> ttiTmp) {
		ArrayList<TTI> itr = ttiTmp;
		ArrayList<SB> itr1 = null;
		List<Double> aux = new ArrayList<Double>();

		for (int i = 0; i < itr.size(); i++) {
			TTI element = itr.get(i);
			itr1 = element.getSchedulingBlocks();
			for (int j = 0; j < itr1.size(); j++) {
				SB element1 = itr1.get(j);
				aux.add((double) element1.getPacket().getUser());
			}
		}
		return aux;
	}

	/**
	 * Plota SBs por Usuarios
	 * 
	 * @param ttiTmp
	 */
	public static void plotSBxUser(ArrayList<TTI> ttiTmp) {

		ArrayList<TTI> dataTmp = ttiTmp;
		List<Double> aux1 = getSBUser(dataTmp);
		Set<Double> uniqueSet = new HashSet<Double>(aux1);
		// double meanSBsRate = calcMeanRate(aux);
		double[][] dataToPlot = new double[Constants.getN_USERS()][2];

		int x = 0;
		for (Double temp : uniqueSet) {
			dataToPlot[x][0] = temp;
			dataToPlot[x][1] = Collections.frequency(aux1, temp);
			x++;
		}

		JavaPlot p = new JavaPlot();
		DataSetPlot s = new DataSetPlot(dataToPlot);
		s.setTitle("SBs");
		
		p.setTitle("An�lise de SBs x Usu�rios");
		p.getAxis("y").setLabel("N de SBs");
		p.getAxis("x").setLabel("Usu�rio");
		p.getAxis("y").setBoundaries(0, aux1.size());
		p.getAxis("x").setBoundaries(-1, Constants.getN_USERS()+1);

		PlotStyle myPlotStyle = new PlotStyle();
		myPlotStyle.setStyle(Style.IMPULSES);
		
		myPlotStyle.setLineType(2);
		myPlotStyle.setLineWidth(10);
		
		s.setPlotStyle(myPlotStyle);

		p.addPlot(s);
		p.newGraph();
		p.plot();
	}

	/**
	 * Recebe uma simula��o e retorna um array com meanCqi de cada SB
	 * 
	 * @param ttiTmp
	 * @return
	 */
	public static List<Double> getSBMCS(ArrayList<TTI> ttiTmp) {
		ArrayList<TTI> itr = ttiTmp;
		ArrayList<SB> itr1 = null;
		List<Double> aux = new ArrayList<Double>();

		for (int i = 0; i < itr.size(); i++) {
			TTI element = itr.get(i);
			itr1 = element.getSchedulingBlocks();
			for (int j = 0; j < itr1.size(); j++) {
				SB element1 = itr1.get(j);
				aux.add((double) element1.getPacket().getMean_cqi());
			}
		}
		return aux;
	}

	public static void plotSBxMCS(ArrayList<TTI> ttiTmp) {

		ArrayList<TTI> dataTmp = ttiTmp;
		List<Double> aux = getSBMCS(dataTmp);
		Set<Double> uniqueSet = new HashSet<Double>(aux);
		double[][] dataToPlot = new double[15][2];

		int x = 0;
		for (Double temp : uniqueSet) {
			dataToPlot[x][0] = temp;
			dataToPlot[x][1] = Collections.frequency(aux, temp);
			x++;
		}

		JavaPlot p = new JavaPlot();
		DataSetPlot s = new DataSetPlot(dataToPlot);

		p.setTitle("Resultados Parciais");
		p.getAxis("y").setLabel("N de SBs");
		p.getAxis("x").setLabel("MCS");
		p.getAxis("y").setBoundaries(0.0, aux.size());
		p.getAxis("x").setBoundaries(0.0, 15);

		PlotStyle myPlotStyle = new PlotStyle();
		myPlotStyle.setStyle(Style.IMPULSES);
		myPlotStyle.setLineWidth(10);
		s.setTitle("SBs");
		s.setPlotStyle(myPlotStyle);

		p.addPlot(s);
		p.newGraph();
		p.plot();
	}
	
	/**
	 * Plota os status de uma simula��o, recebe a simula��o por par�metro <br>
	 * e plota a quantidade de usu�rios que : <br>
	 * 
	 * foram atendidos x n�o foram atendidos
	 * 
	 * @param allPkts
	 * @param ttiTmp
	 */
	public static void plotAllocationStatus(ArrayList<Packet> allPkts, ArrayList<TTI> ttiTmp) {

		ArrayList<TTI> ttiIterator = ttiTmp;
		ArrayList<SB> sbIterator = null;
		ArrayList<Packet> naoAlocados = new ArrayList<Packet>(allPkts);
		ArrayList<Packet> alocados = new ArrayList<Packet>();
		
		ArrayList<Packet> t1_Alocado = new ArrayList<Packet>();
		ArrayList<Packet> t2_Alocado = new ArrayList<Packet>();
		ArrayList<Packet> t3_Alocado = new ArrayList<Packet>();
		ArrayList<Packet> t4_Alocado = new ArrayList<Packet>();
		ArrayList<Packet> t1_NaoAlocado = new ArrayList<Packet>();
		ArrayList<Packet> t2_NaoAlocado = new ArrayList<Packet>();
		ArrayList<Packet> t3_NaoAlocado = new ArrayList<Packet>();
		ArrayList<Packet> t4_NaoAlocado = new ArrayList<Packet>();
		
		for (int i = 0; i < ttiIterator.size(); i++) {
			TTI ttiAtual = ttiIterator.get(i);
			sbIterator = ttiAtual.getSchedulingBlocks();

			for (int j = 0; j < sbIterator.size(); j++) {
				SB sbAtual = sbIterator.get(j);
				for (Packet u : allPkts) {
					if (u.getId() == sbAtual.getPacket().getId()) {
						alocados.add(u);
						naoAlocados.remove(u);
						break;
					}
				}
			}
		}
		
		for(Packet p : alocados){
			if(p.getTos()==1)
				t1_Alocado.add(p);
			if(p.getTos()==2)
				t2_Alocado.add(p);
			if(p.getTos()==3)
				t3_Alocado.add(p);
			if(p.getTos()==4)
				t4_Alocado.add(p);
		}
		
		for(Packet p : naoAlocados){
			if(p.getTos()==1)
				t1_NaoAlocado.add(p);
			if(p.getTos()==2)
				t2_NaoAlocado.add(p);
			if(p.getTos()==3)
				t3_NaoAlocado.add(p);
			if(p.getTos()==4)
				t4_NaoAlocado.add(p);
		}
		
		//System.out.println("tipo 1 - Alocados "+t1_Alocado.size()+"--"+"Nao Alocados "+t1_NaoAlocado.size());
		//System.out.println("tipo 2 - Alocados "+t2_Alocado.size()+"--"+"Nao Alocados "+t2_NaoAlocado.size());
		//System.out.println("tipo 3 - Alocados "+t3_Alocado.size()+"--"+"Nao Alocados "+t3_NaoAlocado.size());
		//System.out.println("tipo 4 - Alocados "+t4_Alocado.size()+"--"+"Nao Alocados "+t4_NaoAlocado.size());

		double[][] data_1 = new double[1][2];
		double[][] data_2 = new double[1][2];

		data_1[0][0] = 0;
		data_1[0][1] = naoAlocados.size();

		data_2[0][0] = 0.009;
		data_2[0][1] = alocados.size();

		JavaPlot p = new JavaPlot();
		PlotStyle myPlotStyle = new PlotStyle();
		myPlotStyle.setStyle(Style.IMPULSES);
		myPlotStyle.setLineWidth(5);
		DataSetPlot s = new DataSetPlot(data_1);
		DataSetPlot q = new DataSetPlot(data_2);
		s.setTitle("N�o Alocado");
		q.setTitle("Alocado");
		s.setPlotStyle(myPlotStyle);
		q.setPlotStyle(myPlotStyle);

		p.setMultiTitle("An�lise do status de aloca��o por tipo de servi�o");
		p.getAxis("y").setLabel("Num TOTAL de Pacotes");

		p.getAxis("y").setBoundaries(0, Math.max(alocados.size(), naoAlocados.size()) + 50);
		p.getAxis("x").setBoundaries(-0.1, 0.1);

		p.addPlot(s);
		p.addPlot(q);
		p.newGraph();
		
		//Gera graficos por tipo
		int t1=1,t2=2,t3=3,t4=4;
		generate_subgraph(t2_Alocado, t1_NaoAlocado, p, t1);
		generate_subgraph(t2_Alocado, t2_NaoAlocado, p, t2);
		generate_subgraph(t2_Alocado, t3_NaoAlocado, p, t3);
		generate_subgraph(t2_Alocado, t4_NaoAlocado, p, t4);
		
		/******/
		
		p.plot();
		
	}

	/**
	 * Gera subgr�ficos no plot do status da aloca��o
	 * @param type_x_Alocado
	 * @param type_x_NaoAlocado
	 * @param p = plot
	 * @param t = tipo
	 */
	public static void generate_subgraph(ArrayList<Packet> type_x_Alocado,
			ArrayList<Packet> type_x_NaoAlocado, JavaPlot p, int t) {
		double[][] d1 = new double[1][2];
		double[][] d2 = new double[1][2];

		//Nao alocados
		d1[0][0] = 0;
		d1[0][1] = type_x_NaoAlocado.size();

		//Alocados
		d2[0][0] = 0.009;
		d2[0][1] = type_x_Alocado.size();
		
		DataSetPlot v1 = new DataSetPlot(d1);
		DataSetPlot v2 = new DataSetPlot(d2);
		
		p.getAxis("y").setLabel("Num de pacotes do tipo "+t);
		
		v1.setTitle("N�o Alocado");
		v2.setTitle("Alocado");

		PlotStyle plotStyle1 = new PlotStyle();
		plotStyle1.setStyle(Style.IMPULSES);
		plotStyle1.setLineWidth(7);
		
		v1.setPlotStyle(plotStyle1);
		v2.setPlotStyle(plotStyle1);

		p.addPlot(v1);
		p.addPlot(v2);
		p.newGraph();
	}
}