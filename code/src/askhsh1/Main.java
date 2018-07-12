//Manwlhs Petrakos AM: 2014030009

package askhsh1;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

	RandomAccessFile file;
	int numberOfKeys;
	int numberOfPages;
	int keysPerPage;
	int[] questionTable;
	int[] SortedQuestionTable;

	public Main() {
	}

	private String createFile() throws IOException {

		file = new RandomAccessFile("C:\\file", "rw");
		// don't create file if it already exists
		if (new File("C:\\file").length() > 0) {
			numberOfKeys = (int) file.length() / 4;
			numberOfPages = (int) file.length() / 512;
			keysPerPage = numberOfKeys / numberOfPages;
			return "File already exists.";
		}
		int key = 0;
		long page = 0;

		while (key <= 10000000) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(bos);
			// 128 keys per page
			for (int i = 0; i < 128; i++) {
				// na mhn dimiourgi8oun panw apo 10^7 stoixeia
				if (key <= 10000000) {
					key++;
					out.writeInt(key);
				}
			}
			out.close();
			// Get the bytes of the serialized object
			// and write to the file
			file.seek(page * bos.toByteArray().length);
			file.write(bos.toByteArray());
			page++;
		}
		numberOfKeys = (int) file.length() / 4;
		numberOfPages = (int) file.length() / 512;
		keysPerPage = numberOfKeys / numberOfPages;

		return "File created.";

	}

	private void buildQuestionTable() throws IOException {
		questionTable = new int[10000];
		SortedQuestionTable = new int[10000];
		// fill and sort question tables
		for (int i = 0; i < 10000; i++) {
			// random number from 1 to max number of keys
			// ThreadLocalRandom.current().nextInt(x, y); is not
			// inclusive for y so put y + 1
			questionTable[i] = ThreadLocalRandom.current().nextInt(1, (int) ((file.length() / 4) + 1));
			SortedQuestionTable[i] = questionTable[i];
		}
		Arrays.sort(SortedQuestionTable);
	}

	private boolean binarySearchInPage(byte[] b, int key) throws IOException {
		int min = 0;
		int max = keysPerPage - 1;
		boolean found = false;
		byte[] medNum = new byte[4];
		while (!(min > max) && !found) {
			int med = Math.floorDiv(min + max, 2);
			// med number isolation
			medNum[0] = b[med * 4];
			medNum[1] = b[med * 4 + 1];
			medNum[2] = b[med * 4 + 2];
			medNum[3] = b[med * 4 + 3];
			// check for key
			if (ByteBuffer.wrap(medNum).getInt() == key)
				found = true;
			else if (ByteBuffer.wrap(medNum).getInt() < key)
				min = med + 1;
			else if (ByteBuffer.wrap(medNum).getInt() > key)
				max = med - 1;
		}
		return found;
	}

	private String serialSearch() throws IOException {
		int numOfDiskAccesses = 0;
		int key;
		int currentPage;
		boolean found;

		for (int numOfSearchedNumbers = 0; numOfSearchedNumbers < 10000; numOfSearchedNumbers++) {
			key = 0;
			currentPage = 0;
			found = false;
			while ((key <= file.length() / 4) && (found == false)) {
				// Read from file
				byte[] buffer = new byte[512];
				file.seek(currentPage * buffer.length);
				file.read(buffer);
				currentPage++;
				// search in page
				found = binarySearchInPage(buffer, questionTable[numOfSearchedNumbers]);
			}
			numOfDiskAccesses += currentPage;
		}
		return Integer.toString(numOfDiskAccesses / 10000);
	}

	private String binarySearch() throws IOException {
		int numOfDiskAccesses = 0;
		byte[] num = new byte[4];
		boolean found;
		int minPage;
		int maxPage;
		int middlePage;

		for (int numOfSearchedNumbers = 0; numOfSearchedNumbers < 10000; numOfSearchedNumbers++) {

			found = false;
			minPage = 0;
			maxPage = numberOfPages - 1;
			// if min <= max there is not such key
			while (!(minPage > maxPage) && !found) {
				// page to be searched
				middlePage = Math.floorDiv(minPage + maxPage, 2);
				// Read from file
				byte[] buffer = new byte[512];
				file.seek(middlePage * buffer.length);
				file.read(buffer);
				numOfDiskAccesses++;
				// search in page
				found = binarySearchInPage(buffer, questionTable[numOfSearchedNumbers]);
				if (!found) {
					// go to first integer
					num[0] = buffer[0];
					num[1] = buffer[1];
					num[2] = buffer[2];
					num[3] = buffer[3];
					// random number is in a previous page
					if (ByteBuffer.wrap(num).getInt() > questionTable[numOfSearchedNumbers])
						maxPage = middlePage - 1;
					// go to the last integer
					num[0] = buffer[buffer.length - 4];
					num[1] = buffer[buffer.length - 3];
					num[2] = buffer[buffer.length - 2];
					num[3] = buffer[buffer.length - 1];
					// random number is in a next page
					if (ByteBuffer.wrap(num).getInt() < questionTable[numOfSearchedNumbers])
						minPage = middlePage + 1;
				}
			}
		}
		return Float.toString((float) (numOfDiskAccesses) / 10000);
	}

	private String groupingQuestions() throws IOException {
		int numOfDiskAccesses = 0;
		byte[] num = new byte[4];
		boolean found;
		int minPage;
		int maxPage;
		int middlePage;

		// search
		for (int numOfSearchedNumbers = 0; numOfSearchedNumbers < 10000; numOfSearchedNumbers++) {
			found = false;
			minPage = 0;
			maxPage = numberOfPages - 1;

			// if min <= max there is not such key
			while (!(minPage > maxPage) && (found == false)) {
				// page to be searched
				middlePage = Math.floorDiv(minPage + maxPage, 2);
				// Read from file
				byte[] buffer = new byte[512];
				file.seek(middlePage * buffer.length);
				file.read(buffer);
				numOfDiskAccesses++;
				// search in page
				found = binarySearchInPage(buffer, SortedQuestionTable[numOfSearchedNumbers]);

				if (found == false) {
					// go to first integer
					num[0] = buffer[0];
					num[1] = buffer[1];
					num[2] = buffer[2];
					num[3] = buffer[3];
					// random number is in a previous page
					if (ByteBuffer.wrap(num).getInt() > SortedQuestionTable[numOfSearchedNumbers])
						maxPage = middlePage - 1;
				}
				// go to the last integer
				// this part is out of the if because we need the last
				// integer no matter the state of found
				num[0] = buffer[buffer.length - 4];
				num[1] = buffer[buffer.length - 3];
				num[2] = buffer[buffer.length - 2];
				num[3] = buffer[buffer.length - 1];
				// random number is in a next page
				if (!found && ByteBuffer.wrap(num).getInt() < SortedQuestionTable[numOfSearchedNumbers])
					minPage = middlePage + 1;

				// if next numbers are smaller or same than the last number
				// in the page they are in too
				if (numOfSearchedNumbers < 9999) {
					// while is in the if because [numOfSearchedNumbers + 1]
					// can get out of bounds
					while ((found)
							&& (SortedQuestionTable[numOfSearchedNumbers + 1] <= ByteBuffer.wrap(num).getInt())
							&& (numOfSearchedNumbers < 9999)) {
						numOfSearchedNumbers++;
						found = binarySearchInPage(buffer, SortedQuestionTable[numOfSearchedNumbers]);
					}
				}
			}
		}
		return Float.toString((float) (numOfDiskAccesses) / 10000);
	}

	private String temporaryMemory(int K) throws IOException {

		Queue<byte[]> pageQueue = new LinkedList<byte[]>();
		int numOfDiskAccesses = 0;
		byte[] num = new byte[4];
		boolean found;
		int minPage;
		int maxPage;
		int middlePage;

		// search
		for (int numOfSearchedNumbers = 0; numOfSearchedNumbers < 10000; numOfSearchedNumbers++) {
			found = false;

			// search in queue
			for (int i = 0; i < pageQueue.size(); i++) {
				// peek at the head and put it back at the start
				if (!found)
					found = binarySearchInPage(pageQueue.peek(), questionTable[numOfSearchedNumbers]);
				// queue have to be left intact so continue till starting point
				pageQueue.add(pageQueue.poll());
			}

			// else search in disk
			minPage = 0;
			maxPage = numberOfPages - 1;
			// if min <= max there is not such key
			while (!(minPage > maxPage) && !found) {

				// page to be searched
				middlePage = Math.floorDiv(minPage + maxPage, 2);
				// Read from file
				byte[] buffer = new byte[512];
				file.seek(middlePage * buffer.length);
				file.read(buffer);
				numOfDiskAccesses++;
				// search in page
				found = binarySearchInPage(buffer, questionTable[numOfSearchedNumbers]);

				// add in queue
				if (found && pageQueue.size() < K)
					pageQueue.add(buffer);
				else if (found) {
					pageQueue.poll();
					pageQueue.add(buffer);
				}

				if (!found) {
					// go to first integer
					num[0] = buffer[0];
					num[1] = buffer[1];
					num[2] = buffer[2];
					num[3] = buffer[3];
					// random number is in a previous page
					if (ByteBuffer.wrap(num).getInt() > questionTable[numOfSearchedNumbers])
						maxPage = middlePage - 1;
				}
				// go to the last integer
				// this part is out of the if because we need the last
				// integer no matter the state of found
				num[0] = buffer[buffer.length - 4];
				num[1] = buffer[buffer.length - 3];
				num[2] = buffer[buffer.length - 2];
				num[3] = buffer[buffer.length - 1];
				// random number is in a next page
				if (!found && ByteBuffer.wrap(num).getInt() < questionTable[numOfSearchedNumbers])
					minPage = middlePage + 1;

				// if next numbers are smaller or same than the largest number
				// in the page they are in too
			}
		}
		return Float.toString((float) (numOfDiskAccesses) / 10000);
	}

	public static void main(String[] args) {

		Main m = new Main();
		try {
			System.out.println(m.createFile());
			m.buildQuestionTable();

		  	System.out.println("Average disk accesses with serial search: " + m.serialSearch());
			System.out.println("Average disk accesses with binary search: " + m.binarySearch());
			System.out.println("Average disk accesses with grouping questions: " + m.groupingQuestions());
			System.out.println("Average disk accesses with temporary memory 1, 50, 100: " + m.temporaryMemory(1) + " "
					+ m.temporaryMemory(50) + " " + m.temporaryMemory(100));

			// close file when finish
			m.file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
