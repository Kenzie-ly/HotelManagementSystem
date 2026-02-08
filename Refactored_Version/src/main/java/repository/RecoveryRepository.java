package repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import classes.Enrollement;
import classes.RecoveryPlan;
import classes.RecoveryTask;

public class RecoveryRepository {

    public static void deleteRecoveryPlanByEnrollment(List<Enrollement> enrollements) throws Exception{
        File originalFile = new File(ResourceManager.getRecoveryPlanPath());
        File tempFile = new File("temp.txt");
        List<String> enrollmentIDs = enrollements.stream().map(Enrollement::getEnrollmentID).toList();

        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            //removing parts
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\t");

                if (!(enrollmentIDs.contains(data[1]))) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } 

        if (!originalFile.delete()) {
            System.out.println("Could not delete original file. Check permissions or if file is open.");
            return; 
        }

        // Safety Check: Rename failed?
        if (!tempFile.renameTo(originalFile)) {
            System.out.println("Could not rename temp file. You might be on a different drive partition.");
        }
    }

    public static RecoveryTask findRecoveryTaskByID(String id){
        try (BufferedReader reader = new BufferedReader(new FileReader(ResourceManager.getRecoveryTaskPath()))) {
            reader.readLine();//for skipping the header
            String line;
            while ((line = reader.readLine()) != null){
                String[] recoveryTaskData = line.split("\t");
                if (recoveryTaskData[0].equals(id)){
                    RecoveryTask recoveryTask = new RecoveryTask(
                        recoveryTaskData[0],
                        recoveryTaskData[1],
                        recoveryTaskData[2]
                    );
                    return recoveryTask;
                }
                
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public static void updateRecoveryPlan(List<RecoveryPlan> modifiedPlans) throws IOException {
        File originalFile = new File(ResourceManager.getRecoveryPlanPath());
        File tempFile = new File("temp.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            //removing parts
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\t");
                Optional<RecoveryPlan> deletedPlan = modifiedPlans.stream()
                    .filter(p -> !(p.getTaskID().equals(data[0])) && p.getEnrollmentID().equals(data[1]))
                    .findFirst();

                if (!(deletedPlan.isPresent())) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } 

        if (!originalFile.delete()) {
            System.out.println("Could not delete original file. Check permissions or if file is open.");
            return; 
        }

        // Safety Check: Rename failed?
        if (!tempFile.renameTo(originalFile)) {
            System.out.println("Could not rename temp file. You might be on a different drive partition.");
        }

        //add the new plans
        addRecoveryPlan(modifiedPlans);
    }


    public static void updateRecoveryTask(RecoveryTask modifiedTask) throws IOException {
        File originalFile = new File(ResourceManager.getRecoveryTaskPath());
        File tempFile = new File("temp.txt");

        // 1. USE TRY-WITH-RESOURCES to auto-close streams
        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) { // removed 'true' (append)

            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\t");

                // CHECK: Is this the user we are updating?
                if (data[0].equals(modifiedTask.getTaskID())) {
                    // YES: Write the NEW data
                    writer.write(formatRecoveryTask(modifiedTask));
                } else {
                    // NO: Copy existing line
                    writer.write(line);
                }
                writer.newLine();
            }
            
        } // <--- Streams close AUTOMATICALLY here. The file lock is released.

        // 2. NOW it is safe to swap the files
        
        // Safety Check: Delete failed?
        if (!originalFile.delete()) {
            System.out.println("Could not delete original file. Check permissions or if file is open.");
            return; 
        }

        // Safety Check: Rename failed?
        if (!tempFile.renameTo(originalFile)) {
            System.out.println("Could not rename temp file. You might be on a different drive partition.");
        }
    }

    public static void addRecoveryTask(RecoveryTask newRecoveryTask) throws IOException{
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(ResourceManager.getRecoveryTaskPath(),true))) {
            bufferedWriter.write(formatRecoveryTask(newRecoveryTask));
            bufferedWriter.newLine();
        }
    }

    public static void addRecoveryPlan(List<RecoveryPlan> recoveryPlans)throws IOException{
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(ResourceManager.getRecoveryPlanPath(),true))) {
            for (var plan: recoveryPlans){
                bufferedWriter.write(formatRecoveryPlan(plan));
                bufferedWriter.newLine();
            }
        }
    }

    public static List<RecoveryTask> loadAllRecoveryTask() {
        List<RecoveryTask> recoveryTasks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ResourceManager.getRecoveryTaskPath()))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] value = line.split("\t");

                recoveryTasks.add(new RecoveryTask(value[0], value[1], value[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recoveryTasks;
    }

    public static List<RecoveryPlan> loadAllRecoveryPlan() {
        List<RecoveryPlan> recoveryPlans = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ResourceManager.getRecoveryPlanPath()))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] value = line.split("\t");

                recoveryPlans.add(new RecoveryPlan(
                    findRecoveryTaskByID(value[0]), 
                    EnrollmentRepository.findEnrollementByID(value[1]))
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recoveryPlans;
    }

    private static String formatRecoveryTask(RecoveryTask recoveryTask){
        return(
            recoveryTask.getTaskID()  + "\t" +
            recoveryTask.getPhase() + "\t" +
            recoveryTask.getTask()
        );
    }

    private static String formatRecoveryPlan(RecoveryPlan recoveryPlan){
        return(
            recoveryPlan.getTaskID() + "\t" +
            recoveryPlan.getEnrollmentID()
        );
    }
}
