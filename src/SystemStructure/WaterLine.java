package SystemStructure;


import java.io.*;
import java.util.*;

public class WaterLine {
    static LinkedList<Edge>[] graph = new LinkedList[100];//״̬ת��ͼ
    static int[][] reserve;//ԤԼ��
    static int collision[];//�����г�ͻ�������
    
    static HashMap<String, Integer> isVisited = new HashMap<>();//�ó�ͻ��������Ƿ��Ѿ������ʣ������˼���
    
    static LinkedList<LinkedList<Integer>> constant_duration_allPath = new LinkedList<>();//����·����Ŀ�������ʱ����״̬����ѵĵ��ȷ���
    static LinkedList<Integer> constantDuration_optimalPath = new LinkedList<>();//��ʱ����״̬����ѵĵ��ȷ���
    
    
    static LinkedList<LinkedList<Integer>> allPath = new LinkedList<>();//���ȷ���
    static LinkedList<Integer> onePath = new LinkedList<>();//һ�ֵ��ȷ���
    static LinkedList<Integer> optimalPath = new LinkedList<>();//������ʱ����״̬����ѵĵ��ȷ���
    
    static String initial_conflict;//��ʼ��ͻ����
    static String start;//Ѱ�һ�·ʱ�趨�Ŀ�ʼ��㣨��ͻ������
    static int save[] = new int[100];//����Ѱ����С��·�����еġ�Ȩ�ء�
    
    static int m, n;//ԤԼ��Ŀ��볤�������У�
    static int conflict_num;//��ͻ��������
    static double ans = Integer.MAX_VALUE;//����ƽ����ʱ
    
    static {
        for (int i = 0; i < 100; i++) {
            graph[i] = new LinkedList<>();
        }
    }
    
    //���ó�ʼ��ͻ����
    private static void setInitial_conflict() {
        int max_count = 0;//��ʼ��ͻ����λ�����ֵ
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (reserve[i][j] == 1) {
                    for (int k = j + 1; k <= n; k++) {
                        if (reserve[i][k] == 1) {
                            collision[k - j] = 1;
                            max_count = Math.max(max_count, k - j);
                        }
                    }
                }
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = max_count; i >= 1; i--) {
            if (collision[i] == 1)
                stringBuilder.append(1);
            else
                stringBuilder.append(0);
        }
        initial_conflict = stringBuilder.toString();
        isVisited.put(initial_conflict, 0);
    }
    
    //��ӡ��ʼ��ͻ����
    public static void showInitial_conflict() {
        System.out.println("��ʼ��ͻ����Ϊ--->" + initial_conflict);
    }
    
    //���ݳ�ʼ��ͻ�����ҵ����н�ֹ������������״̬ת��ͼ
    private static void build_graph() {
        isVisited.put(initial_conflict, 0);//��ʼ��ͻ�����ѱ�����
        bfs();//������ȱ���Ѱ�����г�ͻ����������״̬ת��ͼ
    }
    
    public static void show_graph() {
        System.out.println("------״̬ת��ͼ------");
        for (int i = 0; i < conflict_num; i++) {
            System.out.print("��ͻ����:" + (i + 1) + " :" + graph[i].get(0).name + "  �ó�ͻ������״̬ת�ƣ�");
            for (int j = 1; j < graph[i].size(); j++) {
                Edge edge = graph[i].get(j);
                System.out.print(edge.name + "-->" + edge.weight + "   ");
            }
            System.out.println();
        }
        System.out.println("----------------------");
    }
    
    //�ҵ�graph״̬ת��ͼ�����еıպϻ�·
    private static void findAllClosedLoop() {
        for (int i = 0; i < conflict_num; i++) {
            //���ʸó�ʼ��ͻ����������Ϊ1�������һ�η��ʣ�
            isVisited.replace(graph[i].get(0).name, 1);
            start = graph[i].get(0).name;//�趨��ʼ��㣨��ͻ������
            //�����graph[i].get(0)��ͻ��������״̬ת�ƣ�����dfs�Ҹó�ͻ�����Ļ�·
            for (int j = 1; j < graph[i].size(); j++) {
                String name = graph[i].get(j).name;
                int weight = graph[i].get(j).weight;
                //�ó�ͻ�����ĵ�j��ת��,������λԭ����1�����ܼ򵥵���Ϊ1����Ϊ��������ѭ���Ĵ��ڣ�������ʺ�ͼ�1
                isVisited.replace(name, isVisited.get(name) + 1);
                save[1] = weight;
                dfs(name, weight, 1);
                isVisited.replace(name, isVisited.get(name) - 1);
            }
            isVisited.replace(graph[i].get(0).name, 0);
        }
    }
    
    //���ù�����ȱ�����������graph
    private static void bfs() {
        LinkedList<String> queue = new LinkedList<>();
        queue.offer(initial_conflict);
        int count = 0;
        while (!queue.isEmpty()) {
            String now = queue.poll();
            conflict_num++;//��ͻ����������1
            graph[count].add(new Edge(now, -1));//����graph[count]��ʼ״̬
            char[] now_char = now.toCharArray();
            int now_len = now.length();
            int offset = 0;//��λ
            //����û��ȫ����λʱ��˵��δ�������now��ͻ����
            while (offset <= now_len) {
                while (now_len - offset - 1 >= 0 && now_char[now_len - offset - 1] == '1') {
                    offset++;
                }
                //��ʱ�Ѿ�ȫ����λ��break��Ѱ�Ҷ����е���һ����ͻ����
                if (offset == now_len) {
                    graph[count].add(new Edge(initial_conflict, offset + 1));
                    break;
                }
                offset++;//���򽫸�0�Ƴ�ȥ������initial_conflict��������
                String next = OR_Operation(initial_conflict, now.substring(0, now_len - offset));
                //�½���next��㲢���뵽graph[count]��ȥ
                Edge newEdge = new Edge(next, offset);
                graph[count].add(newEdge);
                //�����ǰδ���ʹ��ó�ͻ������㣬���ʹ���˵���ý���Ѿ�������graph��
                if (!isVisited.containsKey(next)) {
                    queue.offer(next);
                    isVisited.put(next, 0);//�ý�����queue�������isVisited
                }
            }
            count++;
        }
    }
    
    //����������ȱ���ȥѰ�Ҵ�begin��ʼ��begin�����Ļ�·
    private static void dfs(String begin, int sum, int step) {
        //������ĳ����㱻�ظ���η��ʣ�˵����һ����·���������·δ���Ǵ�begin��ʼ��
        if (isVisited.get(begin) > 1) {
            if (start.equals(begin)) {
                saveAllAns(step);
            }
            //������ƽ��������ʱ��С�ķ���
            if (start.equals(begin) && ans > (double) sum / (double) step) {
                ans = (double) sum / (double) step;
                updateAllAns(step);
            } else if (start.equals(begin) && ans == (double) sum / (double) step) {
                //�����˵�ǰƽ��������ʱ��С�ķ��������ǵñ���ͬ���ķ���
                updateAns(step);
            }
        } else {
            //��begin��㿪ʼ��ѡ����һ��״̬ȥת��
            for (int i = 0; i < conflict_num; i++) {
                //�ҵ�graph�е�һ�е�begin���
                if (graph[i].get(0).name.equals(begin)) {
                    for (int j = 1; j < graph[i].size(); j++) {
                        String name = graph[i].get(j).name;
                        int weight = graph[i].get(j).weight;
                        //���ʸýڵ���+1��dfs�����������-1
                        isVisited.replace(name, isVisited.get(name) + 1);
                        //�����beginȥ���ý��ġ�Ȩֵ��
                        save[step + 1] = weight;
                        dfs(name, sum + weight, step + 1);
                        isVisited.replace(name, isVisited.get(name) - 1);
                    }
                    break;
                }
            }
        }
    }
    
    //������ͬ��Ans�����Ǹ���allPath������ͬ������ǽ�ȥ
    private static void updateAns(int step) {
        onePath.clear();
        for (int i = 1; i <= step; i++) {
            onePath.add(save[i]);
        }
        allPath.add(new LinkedList<>(onePath));//����Ȩ����ӽ�ȥ,������������ǳ������OK
    }
    
    //�����ָ�С��ansʱ��ζ�����Ǳ���ȫ��������е�Path���
    private static void updateAllAns(int step) {
        allPath.clear();
        onePath.clear();
        for (int i = 1; i <= step; i++) {
            onePath.add(save[i]);
        }
        allPath.add(new LinkedList<>(onePath));//����Ȩ����ӽ�ȥ
    }
    
    //��������·�������ǵ�ʱ�����������
    private static void saveAllAns(int step) {
        onePath.clear();
        for (int i = 1; i <= step; i++) {
            onePath.add(save[i]);
        }
        int interval = onePath.get(0);
        boolean flag = true;
        for (int i = 1; i < onePath.size(); i++) {
            if (onePath.get(i) - onePath.get(i - 1) != interval) {
                flag = false;
                break;
            }
        }
        if (flag) {
            constant_duration_allPath.add(new LinkedList<>(onePath));
        }
    }
    
    //Ѱ��������ʱ�������ȵ����ŵ��Ȳ���
    private static void selectOptimalPath() {
        if (allPath.size() < 2) {
            optimalPath.addAll(allPath.get(0));
        }
        LinkedList<Integer> queue = new LinkedList<>();
        int j_count = 0;
        int min = allPath.get(0).get(j_count);
        queue.add(0);
        //����һ������С�������±꽻��queue,O(n)����queue�������н�������whileѭ��ȥ����
        for (int i = 1; i < allPath.size(); i++) {
            int temp = allPath.get(i).get(j_count);
            if (temp < min) {
                queue.clear();
                queue.offer(i);
                min = temp;//����min
            } else if (temp == min) {
                queue.offer(i);
            }
        }
        j_count++;
        //һֱ������ֱ���ҵ�Ψһһ�����Ǵ�
        while (queue.size() > 1) {
            int min2 = Integer.MAX_VALUE;
            for (Integer integer : queue) {
                int c = allPath.get(integer).get(j_count);
                min2 = Math.min(c, min2);
            }
            Iterator<Integer> iterator = queue.iterator();
            while (iterator.hasNext()) {
                Integer integer = iterator.next();
                int t = allPath.get(integer).get(j_count);
                if (t > min2) {
                    iterator.remove();
                }
            }
            j_count++;
        }
        optimalPath.addAll(allPath.get(queue.get(0)));
    }
    
    //��ӡ������ʱ�������ȵ����ŵ��Ȳ���
    public static void showAllPath() {
        System.out.println("������ʱ�������ȵ����ŵ��Ȳ���������" + allPath.size() + "�֣�");
        for (int i = 0; i < allPath.size(); i++) {
            System.out.println("���ŵ��ȷ���" + (i + 1) + ": " + Arrays.toString(allPath.get(i).toArray()));
        }
        System.out.println("-->����ƽ���ӳ٣�" + ans);
    }
    
    //Ѱ��ֻ�����ʱ�������ȵ����ŵ��Ȳ���
    private static void select_ConstantDuration_OptimalPath() {
        //������Ϊ�˵�ʱ�������ȿ���static�����ˣ�ֱ�Ӻ������洦������һ��select�����ǳ�����
        LinkedList<Integer> linkedList = new LinkedList<>();//��ŵ�ǰ��ʱ������ƽ��ʱ����С���±�
        double mi = Double.MAX_VALUE;
        for (int i = 0; i < constant_duration_allPath.size(); i++) {
            double count = 0;
            for (int j = 0; j < constant_duration_allPath.get(i).size(); j++) {
                count += constant_duration_allPath.get(i).get(j);
            }
            count = count / (double) constant_duration_allPath.get(i).size();
            if (count < mi) {
                linkedList.clear();
                linkedList.add(i);
                mi = Math.min(mi, count);
            } else if (count == mi) {
                linkedList.add(i);
            }
        }
        //��ʱlinkedlist��ȫ��һϵ���±꣬��Ӧ��constant_duration_allPath��ƽ��ʱ����С����һ��list���±�
        //���ǵ������ʣ�ƽ��ʱ�Ӽ�ʱ��ͬҲ����Ч�ʲ�࣬����ѡ����Ȳ�����С������ǰ����Щ����
        if (linkedList.size() < 2) {
            constantDuration_optimalPath.addAll(constant_duration_allPath.get(linkedList.get(0)));
            return;
        }
        LinkedList<Integer> queue = new LinkedList<>();
        int j_count = 0;
        int min = constant_duration_allPath.get(linkedList.get(0)).get(j_count);
        queue.add(linkedList.get(0));
        //����һ������С�������±꽻��queue,O(n)����queue�������н�������whileѭ��ȥ����
        for (int i = 1; i < linkedList.size(); i++) {
            int temp = constant_duration_allPath.get(linkedList.get(i)).get(j_count);
            ;
            if (temp < min) {
                queue.clear();
                queue.offer(linkedList.get(i));
                min = temp;//����min
            } else if (temp == min) {
                queue.offer(linkedList.get(i));
            }
        }
        j_count++;
        //һֱ������ֱ���ҵ�Ψһһ�����Ǵ�
        while (queue.size() > 1) {
            int min2 = Integer.MAX_VALUE;
            for (Integer integer : queue) {
                int c = constant_duration_allPath.get(linkedList.get(integer)).get(j_count);
                min2 = Math.min(c, min2);
            }
            Iterator<Integer> iterator = queue.iterator();
            while (iterator.hasNext()) {
                Integer integer = iterator.next();
                int t = constant_duration_allPath.get(linkedList.get(integer)).get(j_count);
                if (t > min2) {
                    iterator.remove();
                }
            }
            j_count++;
        }
        constantDuration_optimalPath.addAll(constant_duration_allPath.get(queue.get(0)));
    }
    
    //��ӡֻ�����ʱ�������ȵ����е��Ȳ���
    public static void showConstant_duration_allPath() {
        System.out.println("-----------------------");
        System.out.println("ֻ�����ʱ�������ȵĵ��Ȳ���������" + constant_duration_allPath.size() + "�֣�");
        for (int i = 0; i < constant_duration_allPath.size(); i++) {
            System.out.println("���ȷ���" + (i + 1) + ": " + Arrays.toString(constant_duration_allPath.get(i).toArray()));
        }
        System.out.println("-----------------------");
    }
    
    //��ӡֻ�����ʱ�������ȵ����ŵ��Ȳ���
    public static void showConstantDuration_optimalPath() {
        System.out.print("���ǵ��ִ���������Ʒ��㣬ֻ�����ʱ�������ȵ����ŵ��Ȳ����ǣ�");
        System.out.println(Arrays.toString(constantDuration_optimalPath.toArray()));
        double ans = 0;
        for (Integer integer : constantDuration_optimalPath) {
            ans += integer;
        }
        ans /= constantDuration_optimalPath.size();
        System.out.println("�õ��Ȳ�������ƽ���ӳ٣�" + ans);
        System.out.println("��ʱ����������ǣ�" + (double) 1 / ans + "��t");
    }
    
    //��ӡ������ʱ�������ȵ����ŵ��Ȳ���
    public static void showOptimalPath() {
        System.out.print("������ʱ�������ȵ����ŵ��Ȳ����ǣ�");
        System.out.println(Arrays.toString(optimalPath.toArray()));
        System.out.println("��ʱ����������ǣ�" + (double) 1 / ans + "��t");
    }
    
    //�ַ����Ļ�����
    public static String OR_Operation(String a, String b) {
        int a_len = a.length();
        int b_len = b.length();
        if (a_len == 0 && b_len == 0) {
            return "";
        } else if (a_len == 0 || b_len == 0) {
            return (a_len == 0) ? b : a;
        } else {
            if (a.length() >= b.length()) {
                int a_start = a_len - b_len;
                StringBuilder stringBuilder = new StringBuilder(a.substring(0, a_start));
                for (int i = a_start, j = 0; i < a_len; i++, j++) {
                    if (a.charAt(i) == '0' && b.charAt(j) == '0') {
                        stringBuilder.append(0);
                    } else {
                        stringBuilder.append(1);
                    }
                }
                return stringBuilder.toString();
            } else {
                return OR_Operation(b, a);
            }
        }
    }
    
    //���ļ�����ԤԼ��
    public static boolean fromFileToReserve(String filePath, String outPath) {
        File file = new File(filePath);
        File outFile = new File(outPath);
        if (file.exists() && file.isFile()) {
            try {
                PrintStream printStream = new PrintStream(outFile);
                FileInputStream inputStream = new FileInputStream(file);
                byte[] bytes = new byte[(int) file.length()];
                if (inputStream.read(bytes) == -1) {
                    throw new Exception("��ȡ�ļ�ʧ��");
                }
                String str = new String(bytes);
                String[] split = str.split("\r\n");
                String[][] array = new String[split.length][];
                for (int i = 0; i < split.length; i++) {
                    array[i] = split[i].split(" ");
                }
                m = split.length;
                n = array[0].length;
                reserve = new int[m+1][n+1];
                collision = new int[m * n];
                for (int i = 1; i <= m; i++) {
                    for (int j = 1; j <= n; j++) {
                        reserve[i][j] = Integer.parseInt(array[i-1][j-1]);
                    }
                }
                System.setOut(printStream);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
        }
        return true;
    }
    
    //����̨����ԤԼ��
    public static void fromConsoleToReserve(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("������ԤԼ��Ŀ���볤��");
        m = scanner.nextInt();
        n = scanner.nextInt();
        reserve = new int[m + 1][n + 1];
        collision = new int[m * n];
        System.out.println("������ԤԼ��1��ʾռ�ã�0��ʾ��");
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                reserve[i][j] = scanner.nextInt();
            }
        }
        scanner.close();
    }
    
    public static void main(String[] args) {
        //��1������̨����ԤԼ��
        fromConsoleToReserve();
        //��2�����ļ�����ԤԼ��
//        String filePath = "E:\\Java-WorkSpace\\Non_linear_WaterLine\\src\\SystemStructure\\from.txt";
//        String outPath = "E:\\Java-WorkSpace\\Non_linear_WaterLine\\src\\SystemStructure\\out.txt";
//        if (!fromFileToReserve(filePath, outPath))  return;
        //���ó�ʼ��ͻ����
        setInitial_conflict();
        //��ӡ��ʼ��ͻ����
        showInitial_conflict();
        //�����ɳ�ʼ��ͻ���������г̵�״̬ת��ͼ
        build_graph();
        //��ʾ��״̬ת��ͼ
        show_graph();
        //Ѱ����ѵ��ȷ���
        findAllClosedLoop();
        //��ӡ���
        showAllPath();
        //������Ž�
        selectOptimalPath();
        //��ӡ���Ž�
        showOptimalPath();
        //��ӡ���е�ʱ�����ĵ��ȷ���
        showConstant_duration_allPath();
        //Ѱ��ֻ�����ʱ�������ȵ����ŵ��Ȳ���
        select_ConstantDuration_OptimalPath();
        //��ӡֻ�����ʱ�������ȵ����ŵ��Ȳ���
        showConstantDuration_optimalPath();
    }
}

class Edge {
    String name;
    int weight;
    
    public Edge(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }
}
