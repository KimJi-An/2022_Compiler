import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HulMain {
	public static void main(String[] args) throws IOException {
		File file = new File("./test.hul");    // 읽어올 파일
		FileWriter fw = new FileWriter("./test.c");  // 결과 파일

		/* HulCompiler 객체 생성 후 tokenizer() 호출하여 토큰 자르기 */
		HulCompiler hc = new HulCompiler(file, fw); 
		hc.tokenizer();
	}
}

/*
 * 작성자 : 202002481 김지안
 * 작성한 O/S : Windows 11
 * 사용된 컴파일러 : javac 16.0.2
 */