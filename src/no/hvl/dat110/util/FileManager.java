package no.hvl.dat110.util;


/**
 * @author tdoy
 * dat110 - project 3
 */

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Map;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.Set;

import no.hvl.dat110.middleware.Message;
import no.hvl.dat110.rpc.interfaces.NodeInterface;
import no.hvl.dat110.util.Hash;

public class FileManager {
	
	private BigInteger[] replicafiles;							
	private int numReplicas;									
	private NodeInterface chordnode;
	private String filepath; 									
	private String filename;									
	private BigInteger hash;
	private byte[] bytesOfFile;
	private String sizeOfByte;
	private int nfiles = 4;	
	
	
	private Set<Message> activeNodesforFile = null;
	
	public FileManager(NodeInterface chordnode) throws RemoteException {
		this.chordnode = chordnode;
	}
	
	public FileManager(NodeInterface chordnode, int N) throws RemoteException {
		this.numReplicas = N;
		replicafiles = new BigInteger[N];
		this.chordnode = chordnode;
	}
	
	public FileManager(NodeInterface chordnode, String filepath, int N) throws RemoteException {
		this.filepath = filepath;
		this.numReplicas = N;
		replicafiles = new BigInteger[N];
		this.chordnode = chordnode;
	}
	
	public void createReplicaFiles() {
	 	
		for(int i=0; i<nfiles; i++) {
			String replicafile = filename + i;
			replicafiles[i] = Hash.hashOf(replicafile);	

		}
		//System.out.println("Generated replica file keyids for "+chordnode.getNodeIP()+" => "+Arrays.asList(replicafiles));
	

	}
	
    /**
     * 
     * @param bytesOfFile
     * @throws RemoteException 
     */
    public int distributeReplicastoPeers() throws RemoteException {
    	int counter = 0;
    	
    	Random rnd = new Random();
		int index = rnd.nextInt(Util.numReplicas); 
    	
		// create replicas of the filename
		createReplicaFiles();
    	
		// iterate over the replicas
		for (int i = 0; i < replicafiles.length; i++) {
			BigInteger replica = replicafiles[i];
			// for each replica, find its successor by performing findSuccessor(replica)
			NodeInterface succ = chordnode.findSuccessor(replica);
			
			// call the addKey on the successor and add the replica
			succ.addKey(replica);

			// call the saveFileContent() on the successor
			if (i == index) {
				succ.saveFileContent(filename, replica, bytesOfFile, true);
			} else {
				succ.saveFileContent(filename, replica, bytesOfFile, false);
			}
			
			// increment counter
			counter++;
		}
		
    		
		return counter;
    }
	
	/**
	 * 
	 * @param filename
	 * @return list of active nodes having the replicas of this file
	 * @throws RemoteException 
	 */
	public Set<Message> requestActiveNodesForFile(String filename) throws RemoteException {
		this.filename = filename;
		Set<Message> activeNodes = new HashSet<Message>();
		// Task: Given a filename, find all the peers that hold a copy of this file
		
		// generate the N replicas from the filename by calling createReplicaFiles()
		createReplicaFiles();
		// it means, iterate over the replicas of the file
		for(int i = 0; i < replicafiles.length; i++) {
			BigInteger replica = replicafiles[i];
			// for each replica, do findSuccessor(replica) that returns successor s.
    		NodeInterface successor = chordnode.findSuccessor(replica);
    		// get the metadata (Message) of the replica from the successor, s (i.e. active peer) of the file
    		Message message = successor.getFilesMetadata(replica);
    		activeNodes.add(message);
		}
		
		this.activeNodesforFile = activeNodes;
		
		return activeNodes;
	}
	/**
	 * Find the primary server - Remote-Write Protocol
	 * @return 
	 */
	public NodeInterface findPrimaryOfItem() {

		// Task: Given all the active peers of a file (activeNodesforFile()), find which is holding the primary copy
		
		for(Message msg : activeNodesforFile) {
			if(msg.isPrimaryServer()) {
				return Util.getProcessStub(msg.getNodeIP(), msg.getPort());
			}
		}
		
		
		return null; 
	}
	
    /**
     * Read the content of a file and return the bytes
     * @throws IOException 
     * @throws NoSuchAlgorithmException 
     */
    public void readFile() throws IOException, NoSuchAlgorithmException {
    	
    	File f = new File(filepath);
    	
    	byte[] bytesOfFile = new byte[(int) f.length()];
    	
		FileInputStream fis = new FileInputStream(f);
        
        fis.read(bytesOfFile);
		fis.close();
		
		//set the values
		filename = f.getName().replace(".txt", "");		
		hash = Hash.hashOf(filename);
		this.bytesOfFile = bytesOfFile;
		double size = (double) bytesOfFile.length/1000;
		NumberFormat nf = new DecimalFormat();
		nf.setMaximumFractionDigits(3);
		sizeOfByte = nf.format(size);
		
		System.out.println("filename="+filename+" size="+sizeOfByte);
    	
    }
    
    public void printActivePeers() {
    	
    	activeNodesforFile.forEach(m -> {
    		String peer = m.getNodeIP();
    		String id = m.getNodeID().toString();
    		String name = m.getNameOfFile();
    		String hash = m.getHashOfFile().toString();
    		int size = m.getBytesOfFile().length;
    		
    		System.out.println(peer+": ID = "+id+" | filename = "+name+" | HashOfFile = "+hash+" | size ="+size);
    		
    	});
    }

	/**
	 * @return the numReplicas
	 */
	public int getNumReplicas() {
		return numReplicas;
	}
	
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}
	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
	/**
	 * @return the hash
	 */
	public BigInteger getHash() {
		return hash;
	}
	/**
	 * @param hash the hash to set
	 */
	public void setHash(BigInteger hash) {
		this.hash = hash;
	}
	/**
	 * @return the bytesOfFile
	 */ 
	public byte[] getBytesOfFile() {
		return bytesOfFile;
	}
	/**
	 * @param bytesOfFile the bytesOfFile to set
	 */
	public void setBytesOfFile(byte[] bytesOfFile) {
		this.bytesOfFile = bytesOfFile;
	}
	/**
	 * @return the size
	 */
	public String getSizeOfByte() {
		return sizeOfByte;
	}
	/**
	 * @param size the size to set
	 */
	public void setSizeOfByte(String sizeOfByte) {
		this.sizeOfByte = sizeOfByte;
	}

	/**
	 * @return the chordnode
	 */
	public NodeInterface getChordnode() {
		return chordnode;
	}

	/**
	 * @return the activeNodesforFile
	 */
	public Set<Message> getActiveNodesforFile() {
		return activeNodesforFile;
	}

	/**
	 * @return the replicafiles
	 */
	public BigInteger[] getReplicafiles() {
		return replicafiles;
	}

	/**
	 * @param filepath the filepath to set
	 */
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
}
