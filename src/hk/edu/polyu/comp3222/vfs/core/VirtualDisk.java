package hk.edu.polyu.comp3222.vfs.core;

import com.sun.beans.editors.ByteEditor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.io.File;
import java.util.BitSet;
import java.util.UUID;

/**
 * A virtual disk.
 */

public class VirtualDisk {
    /**
     * Default constructor.
     */


    private static DiskHeader dHeader = new DiskHeader();
    private static NodeBitmap nbitmap = new NodeBitmap();
    private static BlockBitmap bbitmap = new BlockBitmap();
    private static NodeTable ntable = new NodeTable();

    private boolean diskReady = false;
    private Path diskLocation;

    public VirtualDisk(){

    }

    public VirtualDisk(String diskpath)
    {
        this.open(diskpath);
    }

    public boolean createDirectory(String in_directoryname, int in_underDirectory)
    {
        if(diskReady)
        {
            int nodeToUse = nbitmap.findEarliestIdle();
            nbitmap.register(nodeToUse);
            ntable.register(nodeToUse, in_directoryname, 268435456 + in_underDirectory);
            System.out.println("Creating directory '" + in_directoryname + "' with node ID = " + nodeToUse);
            return true;
        }
        return false;
    }

    public boolean moveFile(int in_nodeID, int in_directoryID)
    {
        if(diskReady)
        {
            String fname = ntable.getNodeFileName(in_nodeID);
            ntable.register(in_nodeID, fname, in_directoryID);
            return true;
        }
        return false;
    }

    public boolean renameFile(int in_nodeID, String in_fname)
    {
        if(diskReady)
        {
            int filetype = ntable.getNodeFileType(in_nodeID);
            ntable.register(in_nodeID, in_fname, filetype);
            return true;
        }
        return false;
    }

    public boolean createDisk(String in_diskName, int in_diskSize)
    {
        if(in_diskSize == -1)
        {
            File file = new File ("/");
            long usableSpace = file.getUsableSpace();

            //System.out.println("Space free : " + usableSpace /1024 /1024 + " mb");

            in_diskSize = (int)(usableSpace/(long)1024/(long)1024);
        }

        File newdisk = new File(in_diskName + ".vdisk");
        if(newdisk.exists() && !newdisk.isDirectory())
        {
            // Abort creation if disk image exists
            System.err.println("Disk cannot be created due to duplicated disk name.");
            return false;
        }
        else
        {
            // Prepare disk header
            int blknum = (in_diskSize * 1024)/4;
            int nodenum = 524288;
            NodeTable newNodeTable = new NodeTable();
            newNodeTable.init();
            BitSet nodeBitmap = new BitSet(nodenum);
            BitSet blockBitmap = new BitSet(blknum);
            nodeBitmap.clear();
            nodeBitmap.set(52487);
            blockBitmap.clear();
            blockBitmap.set(blknum-1);
            byte[] datanode = new byte[524288*68];

            byte[] intBuffer;
            int i;

            // Set disk starter
            dHeader.init();

            // Set disk mount state
            dHeader.setMountState(0);

            // Set disk name
            dHeader.setDiskName(in_diskName);

            // Set disk path
            dHeader.setPath(in_diskName);

            // Set disk UUID
            dHeader.setUUID(UUID.randomUUID().toString().replaceAll("-", ""));

            // Set disk total block number
            dHeader.setBlkNum(blknum);



            // Write to vdisk
            try(FileChannel fc = FileChannel.open(Paths.get(in_diskName + ".vdisk"), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE))
            {
                //System.err.println("Size when save : " + blockBitmap.toByteArray().length);
                fc.write(ByteBuffer.wrap(dHeader.getRawBytes())); // write disk header
                fc.write(ByteBuffer.wrap(nodeBitmap.toByteArray())); // write node bitmap
                //System.out.println("size of nodebitmap array = " + nodeBitmap.toByteArray().length);
                fc.write(ByteBuffer.wrap(blockBitmap.toByteArray())); // write block bitmap
                fc.write(ByteBuffer.wrap(newNodeTable.getRawNodeTable())); // write node table
                System.out.println("Data Node position = " + fc.position());
                fc.write(ByteBuffer.wrap(datanode)); // write data node
                //System.out.println("Node table size before save = " + newNodeTable.getRawNodeTable().length);
                //System.out.println("size of blockmap array = " + blockBitmap.toByteArray().length);
                return true;
            }
            catch (Exception e)
            {
                System.err.print("Disk cannot be created.");
                return false;
            }
        }
    }

    public boolean search(String in_kw)
    {
        if(diskReady)
        {
            search Searching = new search();
            Searching.readFromFile(this.diskLocation);
            Searching.searchFile(in_kw);
            return true;
        }
        return false;
    }

    public boolean listAll()
    {
        list Listing = new list();
        Listing.readFromFile(this.diskLocation);
        Listing.list(0);
        return true;
    }

    public boolean listLocation(int in_ID)
    {
        list Listing = new list();
        Listing.readFromFile(this.diskLocation);
        Listing.list(in_ID);
        return true;
    }

    public boolean open(String diskpath)
    {
        Path real_diskpath = Paths.get(diskpath + ".vdisk");
        this.diskLocation = real_diskpath;
        if(validateDisk(real_diskpath))
        {
            //dHeader = readHeader(real_diskpath);
            dHeader.readFromFile(real_diskpath);
            nbitmap.readFromFile(real_diskpath);
            bbitmap.readFromFile(real_diskpath);
            ntable.readFromFile(real_diskpath);
            diskReady = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean validateDisk(Path diskpath) {
        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ)) {
            ByteBuffer buf = ByteBuffer.allocate(8);
            fc.position(0);
            fc.read(buf);
            String tmp_DISK_STARTER = new String(buf.array(), "ASCII");
            if (tmp_DISK_STARTER.equals(dHeader.getDiskStarter())) {
               // System.out.println("Disk Identifier\t\t: " + tmp_DISK_STARTER);
                buf = ByteBuffer.allocate(4);
                fc.read(buf);
                buf.flip();
                int tmp_MountState = buf.getInt();
                if (tmp_MountState == 0) {
                    return true;
                }
                System.err.print("The file specified is being locked by another process and cannot be mounted.");
                return false;
            } else {
                System.err.print("The file specified is not a valid vDisk file.");
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean close()
    {
        dHeader.markInactive();
        diskReady = false;
        return true;
    }

    public boolean disposeDisk(String in_diskName) {
        Path diskpath = Paths.get(in_diskName + ".vdisk");
        if (validateDisk(diskpath)) {
            try {
                Files.delete(diskpath);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean renameDisk(String name)
    {
        if(diskReady)
        {
            dHeader.setDiskName(name);
            dHeader.writeDisk();
            return true;
        }
        return false;
    }

    public boolean showDiskInfo() {
        if (diskReady) {
            dHeader.showInfo();

            //Display used block;
            int totalblk = dHeader.getBlkNum();
            int usedblk = bbitmap.findUsage();
            System.out.println("Used block \t\t\t: " + usedblk + " ( " + usedblk*4096/1024/1024 + " MB of " + totalblk *4096/1024/1024 + " MB in total.");
            return true;
        }
        return false;
    }

    public boolean importFile(String in_filepath)
    {
        if(diskReady)
        {

            //temp
            //int blkneeded = 20;
            byte data[];

            Path filepath = Paths.get(in_filepath);

            System.out.println("Attempting to write file " + in_filepath);
            try (FileChannel fc = FileChannel.open(filepath, StandardOpenOption.READ))
            {
                ByteBuffer buf;
                long filesize = fc.size();
                long original_filesize = fc.size();
                //System.out.println("Original size = " + (int)original_filesize);
                long blkNeeded = 0;
                int nodeToUse = 0;
                int blockToUse = 0;
                //int[] addr = new int[1074791439];
                int[] addr = new int[15]; // Stardard pointer
                int extState = 0;
                int extAddrBlock[] = new int[1024];
                int extAddrBlock2[] = new int[1024];
                int extAddrBlock3[] = new int[1024];
                int extAddrCounter = 0;
                int extAddrCounter2 = 0;
                int extAddrCounter3 = 0;
                System.out.println("Input file size is " + filesize + " byte(s)");
                if(filesize > 4096) {
                    blkNeeded = (filesize / 4096) + 1;
                }
                else
                {
                    blkNeeded = 1;
                }
                System.out.println("Block needed for the storage = " + blkNeeded);

                System.out.println("Finding earliest idle node");
                nodeToUse = nbitmap.findEarliestIdle();
                System.out.println("Found idle node at " + nodeToUse);
                nbitmap.register(nodeToUse);
                System.out.println("Writnig to node table at #" + nodeToUse);
                ntable.register(nodeToUse, in_filepath, 0);

                // Write to actual data node
                DataNode node = new DataNode();
                DataBlock block = new DataBlock();

                node.init(this.diskLocation);
                block.init(this.diskLocation);

                ByteBuffer readFromTarget;
                fc.position(0);

                System.out.println("Attempting to write data block");

                int counter = 0;
                long blkTotal = blkNeeded;
                while (blkNeeded > 0) {
                    if(blkNeeded == 1)
                    {
                        readFromTarget = ByteBuffer.allocate((int)filesize);
                        fc.read(readFromTarget);
                        readFromTarget.flip();
                        System.out.println("Finding earliest idle block");
                        blockToUse = bbitmap.findEarliestIdle();
                        System.out.println("Found idle block at " + blockToUse);
                        bbitmap.register(blockToUse);
                        block.writeBlock(blockToUse, readFromTarget.array());
                        System.out.println("Writing data block address #" + blockToUse + " ( blk " + counter + "/" + blkTotal + " len=" + readFromTarget.array().length + ")");
                        readFromTarget.clear();
                        switch (extState)
                        {
                            case 1:
                            {
                                System.out.println("Writing to extended address block cell " + extAddrCounter + " = " + blockToUse);
                                extAddrBlock[extAddrCounter] = blockToUse;
                                extAddrCounter++;
                                DataBlock extBlock1 = new DataBlock();
                                extBlock1.init(this.diskLocation);
                                byte[] intBuffer = new byte[4096];
                                ByteBuffer intByteBuffer;
                                int bufferpos = 0;
                                for (int i = 0; i < extAddrCounter; i++) {
                                    intByteBuffer = ByteBuffer.allocate(4).putInt(extAddrBlock[i]);
                                    {
                                        intBuffer[bufferpos + 0] = intByteBuffer.array()[0];
                                        intBuffer[bufferpos + 1] = intByteBuffer.array()[1];
                                        intBuffer[bufferpos + 2] = intByteBuffer.array()[2];
                                        intBuffer[bufferpos + 3] = intByteBuffer.array()[3];
                                        //System.out.println("Setting extended data block at #" + bufferpos);
                                        System.out.println("Setting extended data block with address " + extAddrBlock[i] + " total ->  " + extAddrCounter);
                                        bufferpos += 4;
                                    }
                                }
                                System.out.println("Writing extended data block at " + addr[12]);
                                //System.out.println("extAddrCounter = " + extAddrCounter);
                                extBlock1.writeBlock(addr[12], intBuffer);
                                break;
                            }
                            case 2:
                            {
                                int extAddrBLK = bbitmap.findEarliestIdle(); // For Extended pointer 2
                                    bbitmap.register(extAddrBLK);                // Register Extended pointer 2
                                    extAddrBlock[extAddrCounter] = blockToUse;   // Register first block to member of extended pointer 2
                                    ByteBuffer intBuffer;
                                    byte[] intBufferToWrite = new byte[4096];
                                    int loopCount = 0;
                                    for(int n=0;n<4096;n+=4)
                                    {
                                    intBuffer = ByteBuffer.allocate(4).putInt(extAddrBlock[loopCount]);
                                    intBufferToWrite[n+0] = intBuffer.array()[0];
                                    intBufferToWrite[n+1] = intBuffer.array()[1];
                                    intBufferToWrite[n+2] = intBuffer.array()[2];
                                    intBufferToWrite[n+3] = intBuffer.array()[3];
                                    loopCount++;
                                }
                                block.writeBlock(extAddrBLK, intBufferToWrite); // Write extended pointer 2
                                extAddrBlock2[extAddrCounter2] = extAddrBLK;
                                extAddrCounter2++;


                                loopCount =0;
                                for(int n=0;n<4096;n+=4)
                                {
                                    if(extAddrBlock2[loopCount] == 0)
                                    {
                                        //System.out.println("Error in here !!! this is 0 too!!!!");
                                    }
                                    intBuffer = ByteBuffer.allocate(4).putInt(extAddrBlock2[loopCount]);
                                    intBufferToWrite[n+0] = intBuffer.array()[0];
                                    intBufferToWrite[n+1] = intBuffer.array()[1];
                                    intBufferToWrite[n+2] = intBuffer.array()[2];
                                    intBufferToWrite[n+3] = intBuffer.array()[3];
                                    loopCount++;
                                }
                                block.writeBlock(addr[13], intBufferToWrite);

                                break;
                            }
                            case 3:
                            {
                                break;
                            }
                            default:
                            {
                                addr[counter] = blockToUse;
                                break;
                            }
                        }
                        blkNeeded--;
                     }
                     else // Not reaching the last block;
                     {
                         if(counter == 12)
                         {
                             System.out.println("Reached extended pointer 1");
                             System.out.println("Trying to allocate extended pointer 1");
                             blockToUse = bbitmap.findEarliestIdle();
                             addr[12] = blockToUse;
                             System.out.println("Set extended pointer 1 to use " + blockToUse);
                             bbitmap.register(blockToUse);
                             extState = 1;
                         }
                         else if(counter == 1036)
                         {
                             System.out.println("Reached extended pointer 2");
                             extAddrCounter = 0;
                             blockToUse = bbitmap.findEarliestIdle();
                             addr[13] = blockToUse;
                             bbitmap.register(blockToUse);
                             extState = 2;
                         }
                         else if(counter == 1049612)
                         {
                             System.out.println("Reached extended pointer 3");
                             blockToUse = bbitmap.findEarliestIdle();
                             //addr[16] = blockToUse;
                             bbitmap.register(blockToUse);
                             extState = 3;
                         }

                         readFromTarget = ByteBuffer.allocate(4096);
                         fc.read(readFromTarget);
                         readFromTarget.flip();
                         System.out.println("Finding earliest idle block");
                         blockToUse = bbitmap.findEarliestIdle();
                         System.out.println("Found idle block at " + blockToUse);
                         bbitmap.register(blockToUse);
                         block.writeBlock(blockToUse, readFromTarget.array());
                         System.out.println("Writing data block address #" + blockToUse + " ( blk " + counter + "/" + blkTotal + " len=" + readFromTarget.array().length + ")");
                         readFromTarget.clear();

                         switch (extState)
                         {
                             case 1:
                             {
                                 System.out.println("Writing to extended address block cell " + extAddrCounter + " = " + blockToUse);
                                 extAddrBlock[extAddrCounter] = blockToUse;
                                 extAddrCounter++;
                                 if(extAddrCounter == 1024)
                                 {
                                     int extptr1 = bbitmap.findEarliestIdle();
                                     bbitmap.register(extptr1);
                                     addr[12] = extptr1;

                                     ByteBuffer intBuffer;
                                     byte[] intBufferToWrite = new byte[4096];

                                     int loopCount = 0;
                                     for(int n=0;n<4096;n+=4)
                                     {
                                         intBuffer = ByteBuffer.allocate(4).putInt(extAddrBlock[loopCount]);
                                         intBufferToWrite[n+0] = intBuffer.array()[0];
                                         intBufferToWrite[n+1] = intBuffer.array()[1];
                                         intBufferToWrite[n+2] = intBuffer.array()[2];
                                         intBufferToWrite[n+3] = intBuffer.array()[3];
                                         loopCount++;
                                     }

                                     block.writeBlock(addr[12], intBufferToWrite);
                                     extAddrCounter = 0;
                                 }
                                 break;
                             }
                             case 2:
                             {
                                 if(extAddrCounter == 1023)
                                 {
                                     extAddrBlock[extAddrCounter] = blockToUse;
                                     extAddrCounter++;
                                     int extAddrBLK = bbitmap.findEarliestIdle();
                                     bbitmap.register(extAddrBLK);
                                     System.out.println("Registering extended address block to extended pointer 2 on #" + extAddrBLK);
                                     ByteBuffer intBuffer;
                                     byte[] intBufferToWrite = new byte[4096];
                                     int loopCount = 0;
                                     for(int n=0;n<4096;n+=4)
                                     {
                                         intBuffer = ByteBuffer.allocate(4).putInt(extAddrBlock[loopCount]);
                                         intBufferToWrite[n+0] = intBuffer.array()[0];
                                         intBufferToWrite[n+1] = intBuffer.array()[1];
                                         intBufferToWrite[n+2] = intBuffer.array()[2];
                                         intBufferToWrite[n+3] = intBuffer.array()[3];
                                         System.out.println("extAddrBlock[loopCount] = " + extAddrBlock[loopCount]);
                                         loopCount++;
                                     }
                                     block.writeBlock(extAddrBLK, intBufferToWrite);

                                     for(int n=0;n<extAddrBlock.length;n++)
                                     {
                                         extAddrBlock[n] = 0;
                                     }

                                     extAddrBlock2[extAddrCounter2] = extAddrBLK;
                                     extAddrCounter2++;
                                     extAddrCounter = 0;
                                     break;
                                 }
                                 else
                                 {
                                     System.out.println("[!!!] Writing to extended address ( " + extAddrCounter2 + " ) block cell " + extAddrCounter + " = " + blockToUse );
                                     extAddrBlock[extAddrCounter] = blockToUse;
                                     extAddrCounter++;
                                     break;
                                 }

                             }
                             case 3:
                             {
                                 break;
                             }
                             default:
                             {
                                 addr[counter] = blockToUse;
                                 break;
                             }
                         }
                         counter++;
                         filesize -= 4096;
                         blkNeeded--;
                     }
                 }
                 node.writeNode(nodeToUse,(int)original_filesize,addr);
                System.out.println("Extended address count = " + extAddrCounter);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public boolean exportFile(int in_nodeID, String in_filename) {
        if (diskReady) {

            String filename;
            filename = ntable.getNodeFileName(in_nodeID);

            System.out.println("Filename = " + filename);

            DataNode dnode = new DataNode();
            dnode.init(this.diskLocation);
            int[] addr = dnode.readNode(in_nodeID);

            int filesize = addr[0];
            int blkToRead = filesize/4096 + 1;
            int requiredExtendedState = 0;
            System.out.println(blkToRead);

            if(blkToRead>=12 && blkToRead <= 1036)
            {
                requiredExtendedState = 1;
            }
            else if(blkToRead>=1036 && blkToRead <= 1048576)
            {
                requiredExtendedState = 2;
            }
            else if(blkToRead >= 1048576)
            {
                requiredExtendedState = 2;
            }
            else
            {
                requiredExtendedState = 0;
            }

            DataBlock dblock = new DataBlock();
            byte[] dblock_data = new byte[4096];
            dblock.init(this.diskLocation);

            int topLevelAddr = 2;
            int currentBlk = 0;

            try(FileChannel fc = FileChannel.open(Paths.get(in_filename), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                fc.position(0);
                int intCounter = 0;

                switch (requiredExtendedState) {
                    case 0: {

                        while (blkToRead > 0) {

                            if (blkToRead == 1) {
                                dblock_data = dblock.readBlock(addr[topLevelAddr]);
                                topLevelAddr++;
                                int lastBlk = filesize % 4096;
                                byte[] tmpData = new byte[lastBlk];
                                System.out.println("Last block = " + lastBlk);
                                for (int i = 0; i < lastBlk; i++) {
                                    tmpData[i] = dblock_data[i];
                                }
                                fc.write(ByteBuffer.wrap(tmpData));
                                blkToRead--;
                            } else {
                                dblock_data = dblock.readBlock(addr[topLevelAddr]);
                                fc.write(ByteBuffer.wrap(dblock_data));
                                topLevelAddr++;
                                blkToRead--;
                            }

                        }
                        break;
                    }
                    case 1: {
                        for (int i = 0; i < 12; i++) {
                            dblock_data = dblock.readBlock(addr[topLevelAddr]);
                            topLevelAddr++;
                            fc.write(ByteBuffer.wrap(dblock_data));
                            blkToRead--;
                        }

                        int[] addList = new int[1024];
                        byte[] addrListByte = dblock.readBlock(addr[topLevelAddr]);
                        byte[] addrByte = new byte[4];
                        int j = 0;
                        ByteBuffer buf;
                        for (int i = 0; i < 4096; i += 4) {
                            addrByte[0] = addrListByte[i + 0];
                            addrByte[1] = addrListByte[i + 1];
                            addrByte[2] = addrListByte[i + 2];
                            addrByte[3] = addrListByte[i + 3];
                            buf = ByteBuffer.wrap(addrByte);
                            addList[j] = buf.getInt();
                            j++;
                        }


                        while (blkToRead > 0) {

                            if (blkToRead == 1) {
                                dblock_data = dblock.readBlock(addList[intCounter]);
                                topLevelAddr++;
                                int lastBlk = filesize % 4096;
                                byte[] tmpData = new byte[lastBlk];
                                System.out.println("Last block = " + lastBlk);
                                for (int i = 0; i < lastBlk; i++) {
                                    tmpData[i] = dblock_data[i];
                                }
                                fc.write(ByteBuffer.wrap(tmpData));
                                blkToRead--;
                            } else {
                                System.out.println("topleveladdr = " + topLevelAddr);
                                dblock_data = dblock.readBlock(addList[intCounter]);
                                fc.write(ByteBuffer.wrap(dblock_data));
                                blkToRead--;
                                intCounter++;
                            }

                        }
                        blkToRead--;
                        break;


                    }
                    case 2: {
                        for (int i = 0; i < 12; i++) {
                            dblock_data = dblock.readBlock(addr[topLevelAddr]);
                            topLevelAddr++;
                            fc.write(ByteBuffer.wrap(dblock_data));
                            blkToRead--;
                        }

                        // Written first 12 cells

                        int[] addList = new int[1024];
                        byte[] addrListByte = dblock.readBlock(addr[topLevelAddr]);
                        byte[] addrByte = new byte[4];
                        int j = 0;
                        ByteBuffer buf;

                        for (int i = 0; i < 4096; i += 4) {
                            addrByte[0] = addrListByte[i + 0];
                            addrByte[1] = addrListByte[i + 1];
                            addrByte[2] = addrListByte[i + 2];
                            addrByte[3] = addrListByte[i + 3];
                            buf = ByteBuffer.wrap(addrByte);
                            addList[j] = buf.getInt();
                            j++;
                        }

                        for(int u=0;u<1024;u++) {
                            dblock_data = dblock.readBlock(addList[intCounter]);
                            fc.write(ByteBuffer.wrap(dblock_data));
                            blkToRead--;
                            intCounter++;
                        }

                        // Writtten first extended pointer's data

                        System.out.println("Finished reading first 1036 cells");

                        //int extptr2StepCount = (((int) filesize/4096 + 1) - (1024+12)*4096);

                        int extptr2 = addr[15];
                        int ext2ReadCount = 0;
                        System.out.println("Addr for extended pointer 2 = " + extptr2);
                        byte extptr2Byte[] = new byte[4096];
                        byte extptr2IntByte[] = new byte[4];
                        extptr2Byte = dblock.readBlock(extptr2);
                        int extaddr2[] = new int[1024];

                        ByteBuffer intBuffer;
                        int loopCount = 0;

                        for(int n=0;n<4096;n+=4)
                        {
                            extptr2IntByte[0] = extptr2Byte[n+0];
                            extptr2IntByte[1] = extptr2Byte[n+1];
                            extptr2IntByte[2] = extptr2Byte[n+2];
                            extptr2IntByte[3] = extptr2Byte[n+3];
                            extaddr2[loopCount] = ByteBuffer.wrap(extptr2IntByte).getInt();
                            loopCount++;
                        }

                        // Got all address under extended pointer 2


                        while(blkToRead > 0)
                        {
                            if(blkToRead > 1024) {

                                byte addrMember[];
                                addrMember = dblock.readBlock(extaddr2[ext2ReadCount]);
                                //System.out.println("Count me here");
                                ext2ReadCount++;
                                byte addrByte2[] = new byte[4];
                                int extaddr2_member[] = new int[1024];

                                for (int z = 0; z < 1024; z++) {
                                    int m = 0;
                                    for (int i = 0; i < 4096; i += 4) {
                                        addrByte2[0] = addrMember[i + 0];
                                        addrByte2[1] = addrMember[i + 1];
                                        addrByte2[2] = addrMember[i + 2];
                                        addrByte2[3] = addrMember[i + 3];
                                        buf = ByteBuffer.wrap(addrByte2);
                                        extaddr2_member[m] = buf.getInt();
                                        m++;
                                    }

                                }

                                for(int z = 0; z < 1024 ; z++)
                                {
                                    fc.write(ByteBuffer.wrap(dblock.readBlock(extaddr2_member[z])));
                                }
                                blkToRead -= 1024;
                            }
                            else
                            {
                                int remaining = blkToRead;
                                int[] lastBlkSet = new int[remaining];
                                System.out.println("This portion is entered");
                                byte addrMember[];
                                byte tmpInt[] = new byte[4];
                                addrMember = dblock.readBlock(extaddr2[ext2ReadCount]);
                                int convertCount = 0;
                                for(int e=0;e<lastBlkSet.length*4;e+=4)
                                {
                                    tmpInt[0] = addrMember[e+0];
                                    tmpInt[1] = addrMember[e+1];
                                    tmpInt[2] = addrMember[e+2];
                                    tmpInt[3] = addrMember[e+3];
                                    if(e==(lastBlkSet.length*4)-4)
                                    {
                                        lastBlkSet[convertCount] = ByteBuffer.wrap(tmpInt).getInt();
                                        byte lastBlk[] = new byte[filesize%4096];
                                        byte lastBlkTmp[] = new byte[4096];
                                        lastBlkTmp = dblock.readBlock(lastBlkSet[convertCount]);
                                        for(int h=0;h<filesize%4096;h++)
                                        {
                                            lastBlk[h] = lastBlkTmp[h];
                                        }
                                        fc.write(ByteBuffer.wrap(lastBlk));
                                    }
                                    else {
                                        lastBlkSet[convertCount] = ByteBuffer.wrap(tmpInt).getInt();
                                        fc.write(ByteBuffer.wrap(dblock.readBlock(lastBlkSet[convertCount])));
                                    }
                                    convertCount++;

                                }
                                blkToRead-=1024;

                            }
                        }


                    }
                    case 3: {
                        break;
                    }
                    default: {
                        break;
                    }

                }


            }
            catch (IOException e)
            {
                e.printStackTrace();
            }


        }
        return true;
    }



    public boolean removeFile(int in_node)
    {
        if(diskReady)
        {
            nbitmap.unregister(in_node);
            //bbitmap.unregister(in_node);
            ntable.unregister(in_node);
            return true;
        }
        return false;
    }

    public boolean readFile(int node)
    {
        if(diskReady)
        {
            String filename = ntable.getNodeFileName(node);
            System.out.println("Read file " + filename);
            return true;
        }
        return false;
    }

    public boolean debugNode()
    {
        /*
        nbitmap.unregister(0);
        nbitmap.unregister(1);
        nbitmap.unregister(2);
        nbitmap.unregister(3);
        nbitmap.unregister(4);
        nbitmap.unregister(5);
        nbitmap.unregister(6);
        */
        System.out.println("Node end at : " + nbitmap.findEarliestUsed());
        return true;
    }

    public boolean debugBlock()
    {
        /*
        bbitmap.unregister(0);
        bbitmap.unregister(1);
        bbitmap.unregister(2);
        bbitmap.unregister(3);
        bbitmap.unregister(4);
        bbitmap.unregister(5);
        */
        System.out.println("Block end at : " + bbitmap.findEarliestUsed());
        return true;
    }

    public boolean debugNodeTable()
    {
        //System.out.println("Node table size = " + ntable.getRawNodeTable().length);
        //ntable.setNode(15,"filename",0);
        //ntable.setNode(15,"apple.txt",0);
        //System.out.println("Filename = " + ntable.getNodeFileName(2));
        return true;
    }

    public boolean debugDataNode(String diskpath, int in_nodeid)
    {

        DataNode dnode = new DataNode();
        dnode.init(Paths.get(diskpath + ".vdisk"));
        //System.out.println("Data node registered itself at " + dnode.ShowPosition());


        /*
        int testaddress[] = new int[15];
        testaddress[1] = 23692;
        testaddress[2] = 45692;
        testaddress[3] = 57592;
        dnode.writeNode(0,12,testaddress);

        */
        int[] address = dnode.readNode(in_nodeid);

        for(int i=0;i<address.length;i++)
        {
            System.out.println("Result = (" + i + ")" + address[i]);
        }

        // If there is a extended pointer 1
        if(address[14] != 0)
        {
            int[] extaddr1 = new int[address[0]-1024];
            extaddr1 = dnode.readExtendedNode(address[14], address[0] - (12*4096));
            for(int i=0;i<extaddr1.length;i++)
            {
                System.out.println("Result(Ext1) = " + extaddr1[i]);
            }
        }

        return true;
    }

}

class DiskHeader {

    private byte RawDiskHeader[] = new byte[64];
    private String DISK_STARTER = "COMP3222";
    private Path diskpath;

    public void init()
    {
        for(int j=0;j<this.DISK_STARTER.length();j++) {
            RawDiskHeader[j] = this.DISK_STARTER.getBytes()[j];
        }
    }

    public void setPath(String in_diskpath)
    {
        this.diskpath = Paths.get(in_diskpath + ".vdisk");
    }

    public void setMountState(int in_MountState)
    {
        byte[] intBuffer = ByteBuffer.allocate(4).putInt(in_MountState).array();
        int i=8;
        for(int j=0;j<intBuffer.length;j++)
        {
            RawDiskHeader[i] = intBuffer[j];
            i++;
        }
    }

    public void setDiskName(String in_DiskName)
    {
        int i=12;
        for(int j=0;j<16;j++)
        {
            if(j<in_DiskName.length()) {
                RawDiskHeader[i] = in_DiskName.getBytes()[j];
            }
            else
            {
                RawDiskHeader[i] = (byte)0;
            }
            i++;
        }
    }

    public void setUUID(String in_DiskUUID)
    {
        int i=28;
        for(int j=0;j<32;j++)
        {
            if(j<in_DiskUUID.length()) {
                RawDiskHeader[i] = in_DiskUUID.getBytes()[j];
            }
            else
            {
                RawDiskHeader[i] = (byte)0;
            }
            i++;
        }
    }

    public void setBlkNum(int in_BlkNum)
    {
        byte[] intBuffer = ByteBuffer.allocate(4).putInt(in_BlkNum).array();
        int i=60;
        for(int j=0;j<intBuffer.length;j++)
        {
            RawDiskHeader[i] = intBuffer[j];
            i++;
        }
    }

    public int getMountState()
    {
        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ))
        {
            ByteBuffer buf;
            fc.position(8);
            buf = ByteBuffer.allocate(4);
            fc.read(buf);
            buf.flip();
            return buf.getInt();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    public String getDiskName()
    {
        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ))
        {
            ByteBuffer buf;
            fc.position(12);
            buf = ByteBuffer.allocate(16);
            fc.read(buf);
            String diskname = new String(buf.array(), "ASCII");
            return diskname;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public String getUUID()
    {
        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ))
        {
            ByteBuffer buf;
            fc.position(28);
            buf = ByteBuffer.allocate(32);
            fc.read(buf);
            return (new String(buf.array(), "ASCII"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public int getBlkNum()
    {
        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ))
        {
            ByteBuffer buf;
            fc.position(60);
            buf = ByteBuffer.allocate(4);
            fc.read(buf);
            buf.flip();
            return buf.getInt();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    public String getDiskStarter()
    {
        return this.DISK_STARTER;
    }

    public byte[] getRawBytes()
    {
        return this.RawDiskHeader;
    }

    public void readFromFile(Path diskpath) {

        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ)) {
            ByteBuffer buf = ByteBuffer.allocate(RawDiskHeader.length);
            fc.position(0);
            fc.read(buf);
            byte tmp_header[] = buf.array();
            for (int i = 0; i < RawDiskHeader.length; i++) {
                RawDiskHeader[i] = tmp_header[i];
                //System.out.println("Cell " + i + " = " + RawDiskHeader[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.diskpath = diskpath;
    }

    public void markActive()
    {
        byte[] intBuffer = ByteBuffer.allocate(4).putInt(1).array();
        int i=8;
        for(int j=0;j<intBuffer.length;j++)
        {
            RawDiskHeader[i] = intBuffer[j];
            i++;
        }
        this.writeDisk();
    }

    public void markInactive()
    {
        byte[] intBuffer = ByteBuffer.allocate(4).putInt(0).array();
        int i=8;
        for(int j=0;j<intBuffer.length;j++)
        {
            RawDiskHeader[i] = intBuffer[j];
            i++;
        }
        this.writeDisk();
    }

    public void showInfo()
    {
        System.out.println("Disk State\t\t\t: " + this.getMountState());
        System.out.println("Disk Name\t\t\t: " + this.getDiskName());
        System.out.println("Disk UUID\t\t\t: " + this.getUUID());
        System.out.println("Disk Size\t\t\t: " + (this.getBlkNum() * 4) / 1024 + " MB");
        System.out.println("Block Available\t\t\t: " + this.getBlkNum());
    }

    public void writeDisk()
    {
        try(FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ, StandardOpenOption.WRITE))
        {
            fc.position(0);
            fc.write(ByteBuffer.wrap(RawDiskHeader));
            fc.close();
        }
        catch (Exception e)
        {
            System.out.println("Cannot update disk header.");
            e.printStackTrace();
        }
    }

}

class NodeBitmap
{
    private BitSet nodeBitmap = new BitSet(524288);
    private static int PRE_HEADER = 64;
    private Path diskpath;

    public void readFromFile(Path diskpath) {

        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ)) {
            ByteBuffer buf = ByteBuffer.allocate(6561);
            fc.position(PRE_HEADER);
            fc.read(buf);
            //buf.flip();
            byte tmp_bitmap[] = buf.array();
            nodeBitmap = BitSet.valueOf(tmp_bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.err.println("After read : " + nodeBitmap.toByteArray().length);

        this.diskpath = diskpath;
    }

    public void register(int position)
    {
        this.nodeBitmap.set(position);
        this.writeDisk();
    }

    public void unregister(int positoin)
    {
        this.nodeBitmap.clear(positoin);
        this.writeDisk();
    }

    public int findEarliestIdle()
    {
        for(int i=0;i<nodeBitmap.length();i++)
        {
            if(!nodeBitmap.get(i)) {
                return i;
            }
        }
        return -1;
    }

    public int findEarliestUsed()
{
    for(int i=0;i<nodeBitmap.length();i++)
    {
        if(nodeBitmap.get(i)) {
            return i;
        }
    }
    return -1;
}

    private void writeDisk()
    {
        try(FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ, StandardOpenOption.WRITE))
        {
            fc.position(PRE_HEADER);
            fc.write(ByteBuffer.wrap(nodeBitmap.toByteArray()));
            fc.close();
        }
        catch (Exception e)
        {
            System.out.println("Cannot update disk header.");
            e.printStackTrace();
        }
    }

}

class BlockBitmap
{
    private BitSet blockBitmap;
    private int blknum;
    private static int PRE_HEADER = 64 + 6561;
    private Path diskpath;

    public void readFromFile(Path diskpath) {

        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ)) {
            ByteBuffer buf;
            fc.position(60);
            buf = ByteBuffer.allocate(4);
            fc.read(buf);
            buf.flip();
            blknum =  buf.getInt();
            buf = ByteBuffer.allocate(blknum/8);
            fc.position(PRE_HEADER);
            fc.read(buf);
            byte tmp_bitmap[] = buf.array();
            blockBitmap = BitSet.valueOf(tmp_bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.err.println("After read : " + nodeBitmap.toByteArray().length);

        this.diskpath = diskpath;
    }

    public void register(int position)
    {
        this.blockBitmap.set(position);
        this.writeDisk();
    }

    public void unregister(int positoin)
    {
        this.blockBitmap.clear(positoin);
        this.writeDisk();
    }

    public int findEarliestIdle()
    {
        //System.out.println("Block total = " + blockBitmap.length());
        for(int i=0;i<blockBitmap.length();i++)
        {
            if(!blockBitmap.get(i)) {
                return i;
            }
        }
        return -1;
    }

    public int findEarliestUsed()
    {
        //System.out.println("Block total = " + blockBitmap.length());
        for(int i=0;i<blockBitmap.length();i++)
        {
            if(blockBitmap.get(i)) {
                return i;
            }
        }
        return -1;
    }

    public int findUsage()
    {
        //System.out.println("Block total = " + blockBitmap.length());
        int usedCount = 0;
        for(int i=0;i<blockBitmap.length();i++)
        {
            if(blockBitmap.get(i)) {
                usedCount++;
            }
        }
        return usedCount;
    }

    private void writeDisk()
    {
        try(FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ, StandardOpenOption.WRITE))
        {
            fc.position(PRE_HEADER);
            fc.write(ByteBuffer.wrap(blockBitmap.toByteArray()));
            fc.close();
        }
        catch (Exception e)
        {
            System.out.println("Cannot update disk header.");
            e.printStackTrace();
        }
    }

}

class NodeTable
{
    private int PRE_HEADER = 64 + 6561;
    // Disk header 64
    // Node Bitmap 6561

    private Path diskpath;
    private int blknum;
    private byte rawNodeTable[] = new byte[524288*24];

    public void init()
    {

        byte[] intBuffer;
        int j = 0;
        for(int i=0;i<524288;i+=24)
        {
            intBuffer = ByteBuffer.allocate(4).putInt(j).array();
            rawNodeTable[i+0] = intBuffer[0];
            rawNodeTable[i+1] = intBuffer[1];
            rawNodeTable[i+2] = intBuffer[2];
            rawNodeTable[i+3] = intBuffer[3];
            //System.out.println("Set node ID at " + i );
            j++;
        }

    }

    public void readFromFile(Path diskpath) {

        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ)) {
            ByteBuffer buf;
            fc.position(60);
            buf = ByteBuffer.allocate(4);
            fc.read(buf);
            buf.flip();
            blknum =  buf.getInt()/8;
            PRE_HEADER += blknum;
            buf = ByteBuffer.allocate(524288*24);
            //System.out.println("Preheader = " + PRE_HEADER);
            fc.position(PRE_HEADER);
            fc.read(buf);
            buf.flip();
            rawNodeTable = buf.array();
        } catch (IOException e) {
            e.printStackTrace();
        }


        this.diskpath = diskpath;
    }

    public byte[] getRawNodeTable()
    {
        return rawNodeTable;
    }

    public void register(int in_nodeid, String in_filename, int in_filetype)
    {
        byte[] intBuffer = ByteBuffer.allocate(4).putInt(in_filetype).array();
        byte[] filename = in_filename.getBytes();
        int i = 0;

        /*
        intBuffer = ByteBuffer.allocate(4).putInt(in_blockid).array();
        i = ((in_nodeid*24)); // Move position to node starting position
        rawNodeTable[i+0] = intBuffer[0];
        rawNodeTable[i+1] = intBuffer[1];
        rawNodeTable[i+2] = intBuffer[2];
        rawNodeTable[i+3] = intBuffer[3];
        */

        i = ((in_nodeid*24) + 4); // Move position to file name
        for(int j=0;j<16;j++)
        {
            if(j<in_filename.length()) {
                rawNodeTable[i] = in_filename.getBytes()[j];
            }
            else
            {
                rawNodeTable[i] = (byte)0;
            }
            i++;
        }

        i = ((in_nodeid*24) + 4 + 16); // Move position to file type
        //byte[] intBuffer = in_filename.getBytes();

        for (int j = 0; j < intBuffer.length; j++) {
            rawNodeTable[i] = intBuffer[j];
            i++;
        }

        this.writeDisk();

    }

    public void unregister(int in_nodeid)
    {
        String in_filename = "";
        int in_filetype = 0;
        byte[] intBuffer = ByteBuffer.allocate(4).putInt(in_filetype).array();
        byte[] filename = in_filename.getBytes();
        int i = 0;

        i = ((in_nodeid*24) + 4); // Move position to file name
        for(int j=0;j<16;j++)
        {
            if(j<in_filename.length()) {
                rawNodeTable[i] = in_filename.getBytes()[j];
            }
            else
            {
                rawNodeTable[i] = (byte)0;
            }
            i++;
        }

        i = ((in_nodeid*24) + 4 + 16); // Move position to file type
        byte[] strBuffer = in_filename.getBytes();

        for (int j = 0; j < strBuffer.length; j++) {
            rawNodeTable[i] = strBuffer[j];
            i++;
        }

        this.writeDisk();

    }

    public int getNodeFileType(int in_nodeid)
    {
        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ))
        {
            ByteBuffer buf;
            fc.position(PRE_HEADER + (in_nodeid*24) + 4 + 16);
            buf = ByteBuffer.allocate(4);
            fc.read(buf);
            buf.flip();
            return buf.getInt();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    public String getNodeFileName(int in_nodeid)
    {
        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ))
        {
            ByteBuffer buf;
            fc.position(PRE_HEADER + (in_nodeid*24) + 4);
            buf = ByteBuffer.allocate(16);
            fc.read(buf);
            String filename = new String(buf.array(), "ASCII");
            return filename;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    private void writeDisk()
    {
        try(FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ, StandardOpenOption.WRITE))
        {
            fc.position(PRE_HEADER);
            fc.write(ByteBuffer.wrap(rawNodeTable));
            fc.close();
        }
        catch (Exception e)
        {
            System.out.println("Cannot update disk node table.");
            e.printStackTrace();
        }
    }

}

class DataNode
{
    private int PRE_HEADER = 64 + 6561 + (524288*24);
    private Path diskpath;
    private int blknum;

    public void init(Path in_diskpath)
    {
        try (FileChannel fc = FileChannel.open(in_diskpath, StandardOpenOption.READ))
        {
            ByteBuffer buf;
            fc.position(60);
            buf = ByteBuffer.allocate(4);
            fc.read(buf);
            buf.flip();
            this.blknum = buf.getInt();
        }
        catch (IOException e)
        {
            e.printStackTrace();

        }

        PRE_HEADER += blknum/8;

        this.diskpath = in_diskpath;
    }

    public int[] readExtendedNode(int in_nodeid, int bytesToRead)
    {
        int cellToRead = bytesToRead/4096 + 1;
        int[] datanode = new int[cellToRead];
        System.out.println("Cell to read = " + cellToRead);

        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ)) {
            ByteBuffer buf;
            fc.position(PRE_HEADER + (524288*68)+ (in_nodeid*4096));
            System.out.println("Reading extended pointer at #" + (PRE_HEADER + (524288*68)+ (in_nodeid*4096)));
            int j=0;
            for(int i=0;i<cellToRead;i++) {
                buf = ByteBuffer.allocate(4);
                fc.read(buf);
                buf.flip();
                datanode[j] = buf.getInt();
                j++;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return datanode;
    }

    public int[] readNode(int in_nodeid)
    {
        int[] datanode = new int[17];

        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ)) {
            ByteBuffer buf;
            ByteBuffer tmpInt = ByteBuffer.allocate(4);
            int jumppos = (in_nodeid*68);
            fc.position(PRE_HEADER + jumppos);
            System.out.println("Jump " + jumppos);
            int j=0;
            for(int i=0;i<68;i+=4) {
                buf = ByteBuffer.allocate(4);
                fc.read(buf);
                buf.flip();
                datanode[j] = buf.getInt();
                j++;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return datanode;
    }

    public void writeNode(int in_nodeid, int file_size, int[] addr)
    {
        byte[] nodeBuffer = new byte[68];

        for(int u=0; u<addr.length;u++)
        {
            System.out.println("Node data pointer " + u + " = " + addr[u]);
        }

        int i = PRE_HEADER + (in_nodeid*68);

        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            byte[] intBuffer;


            intBuffer = ByteBuffer.allocate(4).putInt(file_size).array();
            for(int j=0;j<intBuffer.length;j++)
            {
                nodeBuffer[j] = intBuffer[j];
                //System.out.println("Setting nodeBuffer " + j + " to " + file_size);
            }

            int w = 8;
            for(int z=0;z<addr.length;z++)
            {
                intBuffer = ByteBuffer.allocate(4).putInt(addr[z]).array();
                for(int x=0;x<intBuffer.length;x++) {
                    nodeBuffer[w] = intBuffer[x];
                    //System.out.println("Setting nodeBuffer " + w + " to " + addr[z]);
                    w++;
                }
            }



            fc.position(i);
            //fc.position(0);

            fc.write(ByteBuffer.wrap(nodeBuffer));
            fc.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Write to data node with ID " + in_nodeid + " , file size = " + file_size + " , address at # " + i);
    }

    public int ShowPosition()
    {
        return this.PRE_HEADER;
    }

    private void read()
    {
    /*
    public void readFromFile(Path diskpath) {

        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ)) {
            ByteBuffer buf;
            fc.position(60);
            buf = ByteBuffer.allocate(4);
            fc.read(buf);
            buf.flip();
            blknum =  buf.getInt()/8;
            PRE_HEADER += blknum;
            buf = ByteBuffer.allocate(524288*24);
            //System.out.println("Preheader = " + PRE_HEADER);
            fc.position(PRE_HEADER);
            fc.read(buf);
            buf.flip();
            rawNodeTable = buf.array();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}

class DataBlock
{
    private int PRE_HEADER = 64 + 6561 + (524288*24) + (524288*68);
    private int blknum = 0;
    private Path diskpath;

    public void init(Path in_diskpath)
    {
        try (FileChannel fc = FileChannel.open(in_diskpath, StandardOpenOption.READ)) {
            ByteBuffer buf;
            fc.position(60);
            buf = ByteBuffer.allocate(4);
            fc.read(buf);
            buf.flip();
            blknum =  buf.getInt()/8;
            PRE_HEADER += blknum;
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.diskpath = in_diskpath;
    }

    public void writeBlock(int in_blkID, byte[] in_datablk)
    {
        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            ByteBuffer buf;
            fc.position(PRE_HEADER + (in_blkID*4096));
            System.out.println("Writing data block at #" + (PRE_HEADER + in_blkID*4096));
            fc.write(ByteBuffer.wrap(in_datablk));
            fc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] readBlock(int in_blkID)
    {
        byte[] datablock = new byte[4096];

        try (FileChannel fc = FileChannel.open(diskpath, StandardOpenOption.READ)) {
            ByteBuffer buf;
            buf = ByteBuffer.allocate(4096);
            System.out.println("Block ID = " + in_blkID);
            fc.position(PRE_HEADER + (in_blkID*4096));
            fc.read(buf);
            buf.flip();
            datablock =  buf.array();
            fc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return datablock;
    }
}