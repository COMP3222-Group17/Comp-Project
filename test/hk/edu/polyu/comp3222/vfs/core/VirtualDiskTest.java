package hk.edu.polyu.comp3222.vfs.core;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by ApplePuffs on 4/3/2017.
 */
public class VirtualDiskTest {

    VirtualDisk disk = new VirtualDisk();

    @Test
    public void testList()
    {
        list List = new list();
        List.readFromFile(Paths.get("sample.vdisk"));

        List.list(0);
        List.listallfiles();
        List.listfolder(0);
    }

    @Test
    public void testBlockBitmap()
    {
        BlockBitmap blockBitmap = new BlockBitmap();
        blockBitmap.readFromFile(Paths.get("sample.vdisk"));
        blockBitmap.register(10000000);
        blockBitmap.unregister(10000000);
        assertNotNull(blockBitmap.findEarliestIdle());
        assertNotNull(blockBitmap.findEarliestUsed());
        assertNotNull(blockBitmap.findUsage());
    }

    @Test
    public void testDataNode()
    {
        DataNode dnode = new DataNode();
        dnode.init(Paths.get("sample.vdisk"));
        dnode.readNode(0);
        dnode.readExtendedNode(0, 0);
        dnode.ShowPosition();
        int addr[] = new int[68];
        //dnode.writeNode(524288,15,addr);
    }

    @Test
    public void testDataBlock()
    {
        DataBlock dataBlock = new DataBlock();
        dataBlock.init(Paths.get("sample.vdisk"));
        assertNotNull(dataBlock.readBlock(0));
        dataBlock.writeBlock(0, dataBlock.readBlock(0));
    }

    @Test
    public void testDiskHeader()
    {
        DiskHeader diskHeader = new DiskHeader();
        diskHeader.readFromFile(Paths.get("sample.vdisk"));
        diskHeader.init();
        assertNotNull(diskHeader.getBlkNum());
        assertNotNull(diskHeader.getDiskName());
        assertNotNull(diskHeader.getDiskStarter());
        assertNotNull(diskHeader.getMountState());
        assertNotNull(diskHeader.getRawBytes());
        assertNotNull(diskHeader.getUUID());
        diskHeader.setDiskName(diskHeader.getDiskName());
        diskHeader.setBlkNum(diskHeader.getBlkNum());
        diskHeader.setUUID(diskHeader.getUUID());
        diskHeader.setMountState(diskHeader.getMountState());
        diskHeader.setPath("sample");
        diskHeader.showInfo();
        diskHeader.markActive();
        diskHeader.markInactive();
    }

    @Test
    public void testNodeBitmap()
    {
        NodeBitmap nodeBitmap = new NodeBitmap();
        nodeBitmap.readFromFile(Paths.get("sample.vdisk"));
        nodeBitmap.register(999999999);
        nodeBitmap.unregister(999999999);
        nodeBitmap.findEarliestUsed();
        assertNotNull(nodeBitmap.findEarliestIdle());
        assertNotNull(nodeBitmap.findEarliestUsed());
    }

    public void testNodeTable()
    {
        NodeTable nodeTable = new NodeTable();
        nodeTable.readFromFile(Paths.get("sample.vdisk"));
        nodeTable.getNodeFileType(0);
        nodeTable.getNodeFileName(0);
        nodeTable.register(9999999, "example", 0);
        nodeTable.unregister(9999999);
        nodeTable.getRawNodeTable();
        nodeTable.init();
        nodeTable.getRawNodeTable();
    }

    @Test
    public void testVirtualDisk()
    {
        VirtualDisk virtualDisk = new VirtualDisk();
        virtualDisk.open("sample");
        assertTrue(virtualDisk.moveFile(0, 0));
        assertTrue(virtualDisk.exportFile(0, "sampleoutput.txt"));
        assertTrue(virtualDisk.listAll());
        assertTrue(virtualDisk.search("sample"));
        assertTrue(virtualDisk.createDirectory("Test directory", 1));
        assertTrue(virtualDisk.debugBlock());
        assertTrue(virtualDisk.debugDataNode("sample", 0));
        assertTrue(virtualDisk.debugNode());
        assertTrue(virtualDisk.debugNodeTable());
        //assertTrue(virtualDisk.importFile("vfs3.iml.txt"));
        assertTrue(virtualDisk.readFile(0));
        assertTrue(virtualDisk.listLocation(0));
        assertTrue(virtualDisk.close());
        virtualDisk.disposeDisk("null");

    }

    @Test
    public void createDisk() throws Exception {


        //disk.createDisk("sample", 1024);
        //disk.open("sample");
        //disk.renameDisk("sample_ok2");
        //disk.showDiskInfo();
        //disk.createDirectory("AppleBB", 0);
        //disk.moveFile(2,4);
        //disk.debugDataNode("sample",2);
        //disk.exportFile(0, "sample2.iml");
        //disk.exportFile(3, "sample_out3.iml");
        //disk.exportFile(0,"logitech2.exe");
        //disk.exportFile(2,"gedit-output.msi");
        //disk.exportFile(3,"intellj.exe");
        //disk.importFile("ideaIC-2017.1.exe");
        //disk.importFile("gedit-x86_64-3.20.1.msi");
        //disk.importFile("vfs2.iml");
        //disk.importFile("vfs.iml");
        //disk.importFile("vfs3.iml.txt");
        //disk.importFile("unifying250.exe");
        //disk.exportFile(5,"netbeans.exe");
        //disk.importFile("netbeans-8.2-javase-windows.exe");
        //disk.importFile("gedit-x86_64.msi");
        //disk.importFile("gedit-output.msi");
        //disk.readFile(4);
        //disk.debugNode();
        //disk.debugBlock();
        //disk.debugNodeTable();
        //disk.close();
        //disk.disposeDisk("sample2");
        //assertTrue(result2);
        //disk.readHeader();


        /*
        search test3 = new search();
        test3.readFromFile("sample");
        //test3.create();
        test3.searchFile("gedit");
        */


        /*
        list test4 = new list();
        test4.readFromFile(Paths.get("sample.vdisk"));
        test4.list(4);
        */

    }


}