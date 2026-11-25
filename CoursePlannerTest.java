import java.util.*;

public class CoursePlannerTest {

    // 1. Course Model
    public static class Course {
        private String code;
        private String title;
        private int credits;
        private List<String> prerequisites;

        public Course(String code, String title, int credits) {
            this.code = code;
            this.title = title;
            this.credits = credits;
            this.prerequisites = new ArrayList<>();
        }

        public void addPrerequisite(String prereqCode) {
            this.prerequisites.add(prereqCode);
        }

        public String getCode() { return code; }
        public String getTitle() { return title; }
        public int getCredits() { return credits; }
        public List<String> getPrerequisites() { return prerequisites; }
    }

    // 2. Course Graph (DAG + Kahn's Algorithm)
    public static class CourseGraph {
        private Map<String, Course> courseMap = new HashMap<>();
        private Map<String, List<String>> adjList = new HashMap<>();

        public void addCourse(Course course) {
            courseMap.put(course.getCode(), course);
            adjList.putIfAbsent(course.getCode(), new ArrayList<>());
        }

        public void addDependency(String prereqCode, String targetCourseCode) {
            if (courseMap.containsKey(prereqCode) && courseMap.containsKey(targetCourseCode)) {
                adjList.get(prereqCode).add(targetCourseCode);
                courseMap.get(targetCourseCode).addPrerequisite(prereqCode);
            }
        }

        public List<Course> getStudyPlan(String targetCode) {
            if (!courseMap.containsKey(targetCode)) return Collections.emptyList();

            Set<String> requiredCodes = new HashSet<>();
            collectPrerequisites(targetCode, requiredCodes);

            Map<String, Integer> inDegree = new HashMap<>();
            for (String code : requiredCodes) inDegree.put(code, 0);

            for (String code : requiredCodes) {
                for (String dependent : adjList.getOrDefault(code, Collections.emptyList())) {
                    if (requiredCodes.contains(dependent)) {
                        inDegree.put(dependent, inDegree.get(dependent) + 1);
                    }
                }
            }

            Queue<String> queue = new LinkedList<>();
            for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
                if (entry.getValue() == 0) queue.offer(entry.getKey());
            }

            List<Course> orderedPlan = new ArrayList<>();
            while (!queue.isEmpty()) {
                String current = queue.poll();
                orderedPlan.add(courseMap.get(current));

                for (String nextCourse : adjList.getOrDefault(current, Collections.emptyList())) {
                    if (requiredCodes.contains(nextCourse)) {
                        inDegree.put(nextCourse, inDegree.get(nextCourse) - 1);
                        if (inDegree.get(nextCourse) == 0) queue.offer(nextCourse);
                    }
                }
            }
            return orderedPlan;
        }

        private void collectPrerequisites(String courseCode, Set<String> visited) {
            if (visited.contains(courseCode)) return;
            visited.add(courseCode);
            Course course = courseMap.get(courseCode);
            if (course != null) {
                for (String prereq : course.getPrerequisites()) {
                    collectPrerequisites(prereq, visited);
                }
            }
        }
    }

    // Main Method
    public static void main(String[] args) {
        CourseGraph graph = new CourseGraph();

        // Load Curriculum Data
        graph.addCourse(new Course("CS101", "Programming Fundamentals", 4));
        graph.addCourse(new Course("MA101", "Discrete Mathematics", 4));
        graph.addCourse(new Course("CS102", "Object-Oriented Programming", 4));
        graph.addCourse(new Course("CS201", "Data Structures and Algorithms", 4));
        graph.addCourse(new Course("AI401", "Machine Learning", 4));

        graph.addDependency("CS101", "CS102");
        graph.addDependency("CS102", "CS201");
        graph.addDependency("MA101", "CS201");
        graph.addDependency("CS201", "AI401");

        // Generate Plan for AI401
        List<Course> plan = graph.getStudyPlan("AI401");
        
        System.out.println("Generated Study Plan for AI401:");
        int step = 1;
        for (Course c : plan) {
            System.out.println(step++ + ". " + c.getCode() + " - " + c.getTitle() + " (" + c.getCredits() + " Credits)");
        }
    }
}