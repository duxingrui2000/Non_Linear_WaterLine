package SystemStructure;


import java.io.*;
import java.util.*;

public class WaterLine {
    static LinkedList<Edge>[] graph = new LinkedList[100];//状态转换图
    static int[][] reserve;//预约表
    static int collision[];//各段中冲突间隔拍数
    
    static HashMap<String, Integer> isVisited = new HashMap<>();//该冲突向量结点是否已经被访问，访问了几次
    
    static LinkedList<LinkedList<Integer>> constant_duration_allPath = new LinkedList<>();//所有路径，目的是求等时间间隔状态下最佳的调度方案
    static LinkedList<Integer> constantDuration_optimalPath = new LinkedList<>();//等时间间隔状态下最佳的调度方案
    
    
    static LinkedList<LinkedList<Integer>> allPath = new LinkedList<>();//调度方案
    static LinkedList<Integer> onePath = new LinkedList<>();//一种调度方案
    static LinkedList<Integer> optimalPath = new LinkedList<>();//允许不等时间间隔状态下最佳的调度方案
    
    static String initial_conflict;//初始冲突向量
    static String start;//寻找环路时设定的开始结点（冲突向量）
    static int save[] = new int[100];//保存寻找最小环路过程中的“权重”
    
    static int m, n;//预约表的宽与长（行与列）
    static int conflict_num;//冲突向量个数
    static double ans = Integer.MAX_VALUE;//最优平均延时
    
    static {
        for (int i = 0; i < 100; i++) {
            graph[i] = new LinkedList<>();
        }
    }
    
    //设置初始冲突向量
    private static void setInitial_conflict() {
        int max_count = 0;//初始冲突向量位数最大值
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
    
    //打印初始冲突向量
    public static void showInitial_conflict() {
        System.out.println("初始冲突向量为--->" + initial_conflict);
    }
    
    //根据初始冲突向量找到所有禁止向量，并建立状态转换图
    private static void build_graph() {
        isVisited.put(initial_conflict, 0);//初始冲突向量已被访问
        bfs();//广度优先遍历寻找所有冲突向量并建立状态转换图
    }
    
    public static void show_graph() {
        System.out.println("------状态转换图------");
        for (int i = 0; i < conflict_num; i++) {
            System.out.print("冲突向量:" + (i + 1) + " :" + graph[i].get(0).name + "  该冲突向量的状态转移：");
            for (int j = 1; j < graph[i].size(); j++) {
                Edge edge = graph[i].get(j);
                System.out.print(edge.name + "-->" + edge.weight + "   ");
            }
            System.out.println();
        }
        System.out.println("----------------------");
    }
    
    //找到graph状态转换图中所有的闭合环路
    private static void findAllClosedLoop() {
        for (int i = 0; i < conflict_num; i++) {
            //访问该初始冲突向量，并置为1（代表第一次访问）
            isVisited.replace(graph[i].get(0).name, 1);
            start = graph[i].get(0).name;//设定开始结点（冲突向量）
            //如果该graph[i].get(0)冲突向量存在状态转移，进入dfs找该冲突向量的环路
            for (int j = 1; j < graph[i].size(); j++) {
                String name = graph[i].get(j).name;
                int weight = graph[i].get(j).weight;
                //该冲突向量的第j个转移,访问置位原数加1，不能简单的置为1，因为可能有自循环的存在，必须访问后就加1
                isVisited.replace(name, isVisited.get(name) + 1);
                save[1] = weight;
                dfs(name, weight, 1);
                isVisited.replace(name, isVisited.get(name) - 1);
            }
            isVisited.replace(graph[i].get(0).name, 0);
        }
    }
    
    //利用广度优先遍历建立整个graph
    private static void bfs() {
        LinkedList<String> queue = new LinkedList<>();
        queue.offer(initial_conflict);
        int count = 0;
        while (!queue.isEmpty()) {
            String now = queue.poll();
            conflict_num++;//冲突向量个数加1
            graph[count].add(new Edge(now, -1));//建立graph[count]初始状态
            char[] now_char = now.toCharArray();
            int now_len = now.length();
            int offset = 0;//移位
            //当还没有全部移位时，说明未处理完该now冲突向量
            while (offset <= now_len) {
                while (now_len - offset - 1 >= 0 && now_char[now_len - offset - 1] == '1') {
                    offset++;
                }
                //此时已经全部移位，break，寻找队列中的下一个冲突向量
                if (offset == now_len) {
                    graph[count].add(new Edge(initial_conflict, offset + 1));
                    break;
                }
                offset++;//否则将该0移出去，并与initial_conflict做或运算
                String next = OR_Operation(initial_conflict, now.substring(0, now_len - offset));
                //新建该next结点并插入到graph[count]中去
                Edge newEdge = new Edge(next, offset);
                graph[count].add(newEdge);
                //如果以前未访问过该冲突向量结点，访问过则说明该结点已经存在于graph，
                if (!isVisited.containsKey(next)) {
                    queue.offer(next);
                    isVisited.put(next, 0);//该结点进入queue，则更新isVisited
                }
            }
            count++;
        }
    }
    
    //利用深度优先遍历去寻找从begin开始到begin结束的环路
    private static void dfs(String begin, int sum, int step) {
        //当出现某个结点被重复多次访问，说明有一个环路，但这个环路未必是从begin开始的
        if (isVisited.get(begin) > 1) {
            if (start.equals(begin)) {
                saveAllAns(step);
            }
            //出现了平均最少延时更小的方案
            if (start.equals(begin) && ans > (double) sum / (double) step) {
                ans = (double) sum / (double) step;
                updateAllAns(step);
            } else if (start.equals(begin) && ans == (double) sum / (double) step) {
                //出现了当前平均最少延时更小的方案，我们得保留同样的方案
                updateAns(step);
            }
        } else {
            //从begin结点开始，选择下一个状态去转移
            for (int i = 0; i < conflict_num; i++) {
                //找到graph中第一列的begin结点
                if (graph[i].get(0).name.equals(begin)) {
                    for (int j = 1; j < graph[i].size(); j++) {
                        String name = graph[i].get(j).name;
                        int weight = graph[i].get(j).weight;
                        //访问该节点则+1，dfs遍历访问完后-1
                        isVisited.replace(name, isVisited.get(name) + 1);
                        //保存从begin去往该结点的“权值”
                        save[step + 1] = weight;
                        dfs(name, sum + weight, step + 1);
                        isVisited.replace(name, isVisited.get(name) - 1);
                    }
                    break;
                }
            }
        }
    }
    
    //出现相同的Ans，我们更新allPath，将相同情况考虑进去
    private static void updateAns(int step) {
        onePath.clear();
        for (int i = 1; i <= step; i++) {
            onePath.add(save[i]);
        }
        allPath.add(new LinkedList<>(onePath));//将该权重添加进去,基础数据类型浅拷贝就OK
    }
    
    //当出现更小的ans时意味着我们必须全部清空所有的Path情况
    private static void updateAllAns(int step) {
        allPath.clear();
        onePath.clear();
        for (int i = 1; i <= step; i++) {
            onePath.add(save[i]);
        }
        allPath.add(new LinkedList<>(onePath));//将该权重添加进去
    }
    
    //保存所有路径（考虑等时间间隔的情况）
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
    
    //寻找允许不等时间间隔调度的最优调度策略
    private static void selectOptimalPath() {
        if (allPath.size() < 2) {
            optimalPath.addAll(allPath.get(0));
        }
        LinkedList<Integer> queue = new LinkedList<>();
        int j_count = 0;
        int min = allPath.get(0).get(j_count);
        queue.add(0);
        //将第一列中最小的数的下标交给queue,O(n)构造queue，其余列交给下面while循环去处理
        for (int i = 1; i < allPath.size(); i++) {
            int temp = allPath.get(i).get(j_count);
            if (temp < min) {
                queue.clear();
                queue.offer(i);
                min = temp;//更新min
            } else if (temp == min) {
                queue.offer(i);
            }
        }
        j_count++;
        //一直往后找直到找到唯一一个就是答案
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
    
    //打印允许不等时间间隔调度的最优调度策略
    public static void showAllPath() {
        System.out.println("允许不等时间间隔调度的最优调度策略有下列" + allPath.size() + "种：");
        for (int i = 0; i < allPath.size(); i++) {
            System.out.println("最优调度方案" + (i + 1) + ": " + Arrays.toString(allPath.get(i).toArray()));
        }
        System.out.println("-->最优平均延迟：" + ans);
    }
    
    //寻找只允许等时间间隔调度的最优调度策略
    private static void select_ConstantDuration_OptimalPath() {
        //懒得再为了等时间间隔调度开新static数组了，直接函数里面处理，与上一个select函数非常类似
        LinkedList<Integer> linkedList = new LinkedList<>();//存放当前等时间间隔的平均时延最小的下标
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
        //此时linkedlist中全是一系列下标，对应着constant_duration_allPath中平均时延最小的那一批list的下标
        //考虑到吞吐率，平均时延及时相同也会有效率差距，尽量选择调度策略中小的数在前的那些策略
        if (linkedList.size() < 2) {
            constantDuration_optimalPath.addAll(constant_duration_allPath.get(linkedList.get(0)));
            return;
        }
        LinkedList<Integer> queue = new LinkedList<>();
        int j_count = 0;
        int min = constant_duration_allPath.get(linkedList.get(0)).get(j_count);
        queue.add(linkedList.get(0));
        //将第一列中最小的数的下标交给queue,O(n)构造queue，其余列交给下面while循环去处理
        for (int i = 1; i < linkedList.size(); i++) {
            int temp = constant_duration_allPath.get(linkedList.get(i)).get(j_count);
            ;
            if (temp < min) {
                queue.clear();
                queue.offer(linkedList.get(i));
                min = temp;//更新min
            } else if (temp == min) {
                queue.offer(linkedList.get(i));
            }
        }
        j_count++;
        //一直往后找直到找到唯一一个就是答案
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
    
    //打印只允许等时间间隔调度的所有调度策略
    public static void showConstant_duration_allPath() {
        System.out.println("-----------------------");
        System.out.println("只允许等时间间隔调度的调度策略有下列" + constant_duration_allPath.size() + "种：");
        for (int i = 0; i < constant_duration_allPath.size(); i++) {
            System.out.println("调度方案" + (i + 1) + ": " + Arrays.toString(constant_duration_allPath.get(i).toArray()));
        }
        System.out.println("-----------------------");
    }
    
    //打印只允许等时间间隔调度的最优调度策略
    public static void showConstantDuration_optimalPath() {
        System.out.print("考虑到现代计算机控制方便，只允许等时间间隔调度的最优调度策略是：");
        System.out.println(Arrays.toString(constantDuration_optimalPath.toArray()));
        double ans = 0;
        for (Integer integer : constantDuration_optimalPath) {
            ans += integer;
        }
        ans /= constantDuration_optimalPath.size();
        System.out.println("该调度策略最优平均延迟：" + ans);
        System.out.println("此时最大吞吐率是：" + (double) 1 / ans + "△t");
    }
    
    //打印允许不等时间间隔调度的最优调度策略
    public static void showOptimalPath() {
        System.out.print("允许不等时间间隔调度的最优调度策略是：");
        System.out.println(Arrays.toString(optimalPath.toArray()));
        System.out.println("此时最大吞吐率是：" + (double) 1 / ans + "△t");
    }
    
    //字符串的或运算
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
    
    //从文件输入预约表
    public static boolean fromFileToReserve(String filePath, String outPath) {
        File file = new File(filePath);
        File outFile = new File(outPath);
        if (file.exists() && file.isFile()) {
            try {
                PrintStream printStream = new PrintStream(outFile);
                FileInputStream inputStream = new FileInputStream(file);
                byte[] bytes = new byte[(int) file.length()];
                if (inputStream.read(bytes) == -1) {
                    throw new Exception("读取文件失败");
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
    
    //控制台输入预约表
    public static void fromConsoleToReserve(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入预约表的宽度与长度");
        m = scanner.nextInt();
        n = scanner.nextInt();
        reserve = new int[m + 1][n + 1];
        collision = new int[m * n];
        System.out.println("请输入预约表，1表示占用，0表示空");
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                reserve[i][j] = scanner.nextInt();
            }
        }
        scanner.close();
    }
    
    public static void main(String[] args) {
        //（1）控制台输入预约表
        fromConsoleToReserve();
        //（2）从文件输入预约表
//        String filePath = "E:\\Java-WorkSpace\\Non_linear_WaterLine\\src\\SystemStructure\\from.txt";
//        String outPath = "E:\\Java-WorkSpace\\Non_linear_WaterLine\\src\\SystemStructure\\out.txt";
//        if (!fromFileToReserve(filePath, outPath))  return;
        //设置初始冲突向量
        setInitial_conflict();
        //打印初始冲突向量
        showInitial_conflict();
        //建立由初始冲突向量构造行程的状态转换图
        build_graph();
        //显示该状态转换图
        show_graph();
        //寻找最佳调度方案
        findAllClosedLoop();
        //打印结果
        showAllPath();
        //求出最优解
        selectOptimalPath();
        //打印最优解
        showOptimalPath();
        //打印所有等时间间隔的调度方案
        showConstant_duration_allPath();
        //寻找只允许等时间间隔调度的最优调度策略
        select_ConstantDuration_OptimalPath();
        //打印只允许等时间间隔调度的最优调度策略
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
