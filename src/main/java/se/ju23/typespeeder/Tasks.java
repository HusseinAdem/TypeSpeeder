package se.ju23.typespeeder;

import jakarta.persistence.*;

import java.util.Collection;

@Entity
public class Tasks {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "task_id", nullable = false)
    private int taskId;
    @Basic
    @Column(name = "task_name", nullable = false, length = 300)
    private String taskName;
    @Basic
    @Column(name = "actual_task", nullable = false, length = 8000)
    private String actualTask;
    @Basic
    @Column(name = "solution", nullable = false, length = 1000)
    private String solution;
    @OneToMany(mappedBy = "tasksByTaskId")
    private Collection<Attempt> attemptsByTaskId;

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getActualTask() {
        return actualTask;
    }

    public void setActualTask(String actualTask) {
        this.actualTask = actualTask;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tasks tasks = (Tasks) o;

        if (taskId != tasks.taskId) return false;
        if (taskName != null ? !taskName.equals(tasks.taskName) : tasks.taskName != null) return false;
        if (actualTask != null ? !actualTask.equals(tasks.actualTask) : tasks.actualTask != null) return false;
        if (solution != null ? !solution.equals(tasks.solution) : tasks.solution != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = taskId;
        result = 31 * result + (taskName != null ? taskName.hashCode() : 0);
        result = 31 * result + (actualTask != null ? actualTask.hashCode() : 0);
        result = 31 * result + (solution != null ? solution.hashCode() : 0);
        return result;
    }

    public Collection<Attempt> getAttemptsByTaskId() {
        return attemptsByTaskId;
    }

    public void setAttemptsByTaskId(Collection<Attempt> attemptsByTaskId) {
        this.attemptsByTaskId = attemptsByTaskId;
    }
}
