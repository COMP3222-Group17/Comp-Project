package hk.edu.polyu.comp3222.vfs.core;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class list {

	private int PRE_HEADER = 64 + 6561;
	// Disk header 64
	// Node Bitmap 6561

	private Path diskpath;
	private int blknum;

	public void readFromFile(Path in_diskpath) {

		this.diskpath = in_diskpath;

		try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ)) {
			ByteBuffer buf;
			fc.position(60);
			buf = ByteBuffer.allocate(4);
			fc.read(buf);
			buf.flip();
			blknum =  buf.getInt()/8;
			this.PRE_HEADER += blknum;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void list(int ID){
		
		if (ID==0){
			listallfiles();
		}
		else{
			listfolder(ID);
		}
		
		
	}
	
	public void listallfiles(){

		
		//should be 524288
		int maxNodeTable = 524288;
	    
		try(FileChannel fc = FileChannel.open(this.diskpath, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE))
				{	
					ByteBuffer buf4 = ByteBuffer.allocate(4);
					ByteBuffer buf16 = ByteBuffer.allocate(16);
					ByteBuffer buf42 = ByteBuffer.allocate(4);
					int position = this.PRE_HEADER;
					
						for(int i = 0;i<maxNodeTable;i++){
						buf4.clear();
						buf16.clear();
						buf42.clear();
						
						fc.position(position);
						
						fc.read(buf4);
						buf4.flip();
						int fileID = buf4.getInt();
						
						fc.read(buf16);
						String filename = new String(buf16.array(), "ASCII");
						filename = filename.replaceAll(String.valueOf((char) 0), "");
						//System.out.println("Filename get= "+filename);
						   
						fc.read(buf42);
						buf42.flip();
						int filetype = buf42.getInt() - 268435456;												
						
							if(filename.length() ==0 ){
								//System.out.println("No files stored in here.");
								position = position +24;
							}
							else{
								System.out.print("ID: " + fileID);
								if(filetype<0)
								{
								System.out.println("   Filename: " + filename);
								position = position +24;
								}
								if(filetype >=0)
								{
								System.out.println("  Foldername: " + filename);
								position = position +24;
										
								}
							}
						}
				}
				catch(IOException e){
				}
	}
	
	public void listfolder(int ID){

		
		//should be 524288
		int maxNodeTable = 524288;
	    
		try(FileChannel fc = FileChannel.open(this.diskpath, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE))
				{	
					ByteBuffer buf4 = ByteBuffer.allocate(4);
					ByteBuffer buf16 = ByteBuffer.allocate(16);
					ByteBuffer buf42 = ByteBuffer.allocate(4);
					int position = this.PRE_HEADER + 20;
					boolean exist = false;
					
						for(int i = 0;i<maxNodeTable;i++){
						buf42.clear();
						
						fc.position(position);
						//System.out.println("Position1: " + position);
						
						fc.read(buf42);
						buf42.flip();
						int fileType = buf42.getInt();
						int file = fileType;
						int folder = fileType - 268435456;												
						
						//System.out.println("Filetype: " + fileType);
						//System.out.println("   File: " + file);
						//System.out.println("   Folder: " + folder);
						
						
						if(ID == file){
							position = position -20; //apple
							fc.position(position);
							buf4.clear();
							buf16.clear();
							
							fc.read(buf4);
							buf4.flip();
							int fileID = buf4.getInt();
							
							fc.read(buf16);
							String filename = new String(buf16.array(), "ASCII");
							filename = filename.replaceAll(String.valueOf((char) 0), "");
							//System.out.println("Filename get= "+filename);
							
							System.out.print("ID: " + fileID);
							System.out.println("   Filename: " + filename);
							position = position +4;
							exist = true;
							
							
						}
						else if(ID == folder){
							position = position -20; //apple
							//System.out.println("Position2: " + position);
							fc.position(position);
							buf4.clear();
							buf16.clear();
							
							fc.read(buf4);
							buf4.flip();
							int fileID = buf4.getInt();
							
							fc.read(buf16);
							String filename = new String(buf16.array(), "ASCII");
							filename = filename.replaceAll(String.valueOf((char) 0), "");
							//System.out.println("Filename get= "+filename);
							
							System.out.print("ID: " + fileID);
							System.out.println("  Foldername: " + filename);
							position = position +4;
							exist = true;
							//System.out.println("Position3: " + position);
						}
						else{
							//System.out.println("Not match ");
							position = position +24;
							
						}
						
						
				
						
						
						}
						
						if (!exist){
							System.out.println("  There are no such file or folder");
						}
				}
				catch(IOException e){
				}
	}
}
