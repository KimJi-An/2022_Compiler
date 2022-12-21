import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

public class HulCompiler {
	File file;
	FileWriter fw;
	int tab = 1;  // 탭 횟수
	int max_index = 0;  // max#에서 # 나타냄
	
	/* 생성자 */
	public HulCompiler(File file, FileWriter fw) {
		this.file = file;
		this.fw = fw;
	}
	
	/* 토큰 자르기 */
	public void tokenizer() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line;
		String input = "";
		
		/* 읽어온 파일의 내용을 모두 한 줄로 이어붙임 */
		while((line = br.readLine()) != null) {
			input += line + " ";
		}

		String[] tokens = input.split("[\t \r]+");  // 탭이나 공백을 기준으로 잘라 배열 tokens에 저장
		
		compile(tokens);  // compile() 호출하여 각 토큰에 해당하는 명령 추가
		br.close();
	}
	
	/* 탭 반복 */
	public void add_tab(int tab) throws IOException {
		String result = "";
		/* tab의 값만큼 result에 탭 추가 */
		for(int i = 0; i < tab; i++) {
			result += "\t";
		}
		fw.write(result);
	}
	
	/* tokens 배열에 저장된 각 토큰들에 해당하는 명령들을 파일에 write */
	public void compile(String[] tokens) throws IOException {
		// 어느 output이나 미리 존재
		fw.write("#include <stdio.h>\n"
				+ "int main() {\n"
				+ "\tint _hul;\n");
		
		Stack<String> max = new Stack<>();  // for문의 max값들(반복 횟수)을 저장할 스택
		
		/* for문의 반복 횟수를 스택에 추가 */
		for(int i = 0; i < tokens.length; i++) {
			if(tokens[i].equals("}")) {
				max.push(tokens[++i]);
			}
		}
		
		int count = max.size();  // 스택의 크기(반복문의 개수)
		String[] max_num = new String[count];  // 각 반복 횟수를 저장할 배열
		
		/* test.c 파일에서 max값 초기화 부분 */
		for(int i = 0; i < count; i++) {
			String num = max.pop();  // 스택에서 반복 횟수 꺼냄
			max_num[i] = num;
			fw.write("\tint max" + String.valueOf(i) + " = " + num + ";\n");  // 반복 횟수 선언
		}
		/* tokens 배열에서 토큰을 하나씩 차례대로 확인 */
		for(int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			
			switch(token) {
				/* 토큰이 'Hul?'인 경우 */ 
				case "Hul?":
					add_tab(tab);  // 들여쓰기 위한 탭 추가
					fw.write("printf(\"input: \");\n");
					add_tab(tab);  // 들여쓰기 위한 탭 추가
					fw.write("scanf(\"%d\", &_hul);\n");  // 정수 읽어 변수에 저장
					break;
				/* 토큰이 'Hul!'인 경우 */
				case "Hul!":
					add_tab(tab);  // 들여쓰기 위한 탭 추가
					fw.write("printf(\"%d\", _hul);\n");  // 변수 출력
					break;
				/* 토큰이 'Hul>'인 경우 */
				case "Hul>":
					add_tab(tab);  // 들여쓰기 위한 탭 추가
					fw.write("_hul++;\n");   // 변수값 1 증가
					break;
				/* 토큰이 'Hul<'인 경우 */
				case "Hul<":
					add_tab(tab);  // 들여쓰기 위한 탭 추가
					fw.write("_hul--;\n");   // 변수값 1 감소
					break;
				/* 토큰이 'Hul{'인 경우 */
				case "Hul{":
					String var = String.valueOf(max_index++);  // i#, max#에서 '#'을 나타냄
					add_tab(tab++);  // 들여쓰기 위한 탭 추가
					fw.write("for (int i" + var + " = 0; i"
							+ var + " < max" + var + "; i" + var + "++) {\n");  // 반복문 수행 
					break;
				/* 토큰이 '}'인 경우 */
				case "}":
					add_tab(--tab);  // 들여쓰기 위한 탭 추가
					fw.write("}\n");  // '}' 추가
					break;
			}
		}	
		fw.write("\treturn 0;\n}");
		fw.close();
	}
}