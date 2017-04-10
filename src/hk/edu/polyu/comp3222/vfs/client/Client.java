package hk.edu.polyu.comp3222.vfs.client;

import hk.edu.polyu.comp3222.vfs.core.VirtualDisk;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.io.File;

/**
 * A virtual disk.
 */


public class Client {
    /**
     * Default constructor.
     */

    private static VirtualDisk vDisk = new VirtualDisk();

    public static void main(String args[]){

        init("sample");

        /*
        System.out.println("FirstTest");
        System.out.print("Please enter command : ");
        Scanner scan = new Scanner(System.in);
        //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        */

        displaydiskmenu();

    }

    public static void init(String in_diskpath)
    {
        vDisk.open(in_diskpath);
    }

    public static void displaydiskmenu(){


        System.out.println("Hello!");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please press enter to use VFS.");
        String testing = scanner.nextLine();


        while(true)
        {
            System.out.flush();
            System.out.println("---------------------------------------------");
            System.out.println("Function List:");
            System.out.println("[1] Create Disk");
            System.out.println("[2] Open Disk");
            System.out.println("[3] Delete Disk");
            System.out.println("[8] Exit");
            System.out.print("Please enter the number of the function:");
            int selection = scanner.nextInt();
            System.out.println("---------------------------------------------");


            switch(selection) {
                case 1:
                    System.out.println("You have select: Create Disk");
                    System.out.print("Please enter a new Disk Name: ");
                    Scanner scanner2 = new Scanner(System.in);
                    String diskname = scanner2.nextLine();

                    System.out.println("You have entered diskname: " + diskname);

                    System.out.println("Please enter the disk space: ");
                    int diskspace = scanner.nextInt();

                    System.out.println("You have entered diskspace: " + diskspace);

                    //call create disk here
                    vDisk.createDisk(diskname, diskspace);
                    break;
                case 2:
                    System.out.println("You have select: Open Disk");
                    System.out.println("Please enter the disk name: ");

                    Scanner scanner3 = new Scanner(System.in);
                    String diskname2 = scanner3.nextLine();

                    System.out.println("You have entered diskname: " + diskname2);
                    //call open disk here
                    init(diskname2);
                    displaymenu();
                    break;
                case 3:
                    System.out.println("You have select: Delete Disk");
                    System.out.println("Please enter the disk name: ");

                    Scanner scanner4 = new Scanner(System.in);
                    String diskname3 = scanner4.nextLine();

                    System.out.println("You have entered diskname: " + diskname3);
                    vDisk.disposeDisk(diskname3);
                    //call delete disk here

                    break;

                case 8:
                    System.out.println("You have select: Exit");
                    System.out.println("Goodbye and have a nice day!");
                    vDisk.close();
                    return ;
                //exit the disk





                default:
                    System.out.println("Sorry please enter vaild input.");
                    //back to main menu
            }
        }

    }

    public static void displaymenu(){


        System.out.println("Welcome back!");
        Scanner scanner = new Scanner(System.in);
		/*System.out.print("Please enter your age...");
		int age = scanner.nextInt();
		System.out.println("What? "+ age +" You mudt be kidding!!");
		*/
        System.out.print("Please press enter to use VFS.");
        String testing = scanner.nextLine();


        while(true)
        {
            System.out.flush();
            System.out.println("---------------------------------------------");
            System.out.println("Function List:");
            System.out.println("[1] Import File");
            System.out.println("[2] Export File");
            System.out.println("[3] Rename File");
            System.out.println("[4] List File");
            System.out.println("[5] Search File");
            System.out.println("[6] Display Disk Information");
            System.out.println("[7] Rename Disk");
            System.out.println("[8] Create Directory");
            System.out.println("[9] Move file");
            System.out.println("[10] Exit");
            System.out.print("Please enter the number of the function:");


            int selection = scanner.nextInt();
            System.out.println("---------------------------------------------");


            switch(selection) {
                case 1:
                    System.out.println("You have select: Import File");
                    System.out.print("Please enter the filename: ");


                    Scanner scanner2 = new Scanner(System.in);
                    String filename = scanner2.nextLine();

                    System.out.println("You have selected file: " + filename);
                    //call import file here
                    vDisk.importFile(filename);
                    break;
                case 2:
                    System.out.println("You have select: Export File");
                    System.out.println("Please enter the file ID : ");

                    Scanner scanner3 = new Scanner(System.in);
                    int fileID = scanner3.nextInt();

                    System.out.println("You have selected file:" + fileID);
                    System.out.print("Please enter the filename the exported file use : ");
                    Scanner scanner31 = new Scanner(System.in);
                    filename = scanner31.nextLine();
                    //call export file here
                    vDisk.exportFile(fileID, filename);
                    break;
                case 3:
                    System.out.println("You have select: Rename File");

                    System.out.print("Please enter the file ID: ");

                    Scanner scanner4 = new Scanner(System.in);
                    //filename = scanner4.nextInt();
                    int fileID2 = scanner4.nextInt();

                    System.out.println("You have selected file: " + fileID2);

                    System.out.print("What do you want to change? ");
                    Scanner scanner41 = new Scanner(System.in);
                    String newname = scanner41.nextLine();

                    //call rename file here
                    vDisk.renameFile(fileID2, newname);
                    System.out.print("File/ Folder "+ fileID2 +" has been changed to "+ newname);
                    break;

                case 4:
                    System.out.println("You have select: List File");
                    System.out.println("---------------------------------------------");
                    System.out.println("Function List:");
                    System.out.println("[1] List all files and folders");
                    System.out.println("[2] List selected folder");
                    System.out.println("[3] Return to main menu");

                    Scanner scanner5 = new Scanner(System.in);
                    int selection2 = scanner5.nextInt();

                    if(selection2 == 1){
                        //Call list all files here.
                        vDisk.listAll();
                    }
                    if(selection2 == 2){
                        System.out.print("Please enter the directory ID: ");
                        Scanner scanner6 = new Scanner(System.in);
                        int dirID = scanner6.nextInt();
                        //Call list select folder here.
                        vDisk.listLocation(dirID);
                    }
                    if(selection2 == 3){
                        //Return to main menu
                    }
                    break;
                case 5:
                    System.out.println("You have select: Search File");

                    Scanner scanner6 = new Scanner(System.in);

                    System.out.print("Please enter the filename/ folder: ");
                    filename = scanner6.nextLine();
                    System.out.println("You have selected file: " + filename);

                    //can call search file here
                    vDisk.search(filename);
                    break;
                case 6:
                    System.out.println("You have select: Display Disk Information");
                    //call display disk here
                    vDisk.showDiskInfo();
                    break;
                case 7:
                    System.out.println("You have select: Rename Disk");

                    Scanner scanner7 = new Scanner(System.in);

                    System.out.print("Pelase enter new disk name: ");
                    String newdiskname = scanner7.nextLine();
                    System.out.println("You have change the disk name to: " + newdiskname);

                    //call rename disk
                    vDisk.renameDisk(newdiskname);

                    break;
                case 8:
                    System.out.println("You have select: Create Directory");
                    Scanner scanner8 = new Scanner(System.in);

                    System.out.print("Please enter new directory name: ");
                    String newdirectoryname = scanner8.nextLine();
                    System.out.println("You created the directory : " + newdirectoryname);

                    System.out.print("Which directory should this directory resides? (0 for default)? : ");
                    int underdir = scanner8.nextInt();

                    // Code
                    vDisk.createDirectory(newdirectoryname, underdir);

                    break;
                case 9:
                    System.out.println("You have select: Rename File");

                    System.out.print("Please enter the file ID: ");

                    Scanner scanner9 = new Scanner(System.in);
                    //filename = scanner4.nextInt();
                    int fileID3 = scanner9.nextInt();

                    System.out.println("You have selected file: " + fileID3);

                    System.out.print("Please enter the directory ID to move to? ");
                    int dirID = scanner9.nextInt();

                    //call rename file here
                    vDisk.moveFile(fileID3, dirID);
                    System.out.println("File ID " + fileID3 + " has been rellocated to directory ID " + dirID);
                    break;
                case 10:
                    System.out.println("You have select: Exit");
                    vDisk.close();
                    return ;
                //exit the disk





                default:
                    System.out.println("Sorry please enter vaild input.");
                    //back to main menu
            }
        }

    }

}
