package ru.hflabs.rcd.task.performer;

import java.util.Date;

/**
 * Класс <class>TaskProgressHolder</class> содержит информацию о текущем прогрессе выполнения задачи
 *
 * @author Nazin Alexander
 */
public class TaskProgressHolder {

    /** Дата */
    public final Date date;
    /** Автор */
    public final String author;

    /** Общее количество шагов */
    public final int totalSteps;
    /** Текущий шаг выполнение */
    private volatile int currentStep;
    /** Текущий процент выполнения */
    private volatile float percent;

    /** Родительский контекст выполнения */
    private TaskProgressHolder parent;

    public TaskProgressHolder(Date date, String author, int totalSteps) {
        this.totalSteps = totalSteps;
        this.currentStep = 0;
        this.percent = 0f;
        this.date = date;
        this.author = author;
    }

    public TaskProgressHolder(int totalSteps, TaskProgressHolder parent) {
        this(parent.date, parent.author, totalSteps);
        this.parent = parent;
    }

    /**
     * Возвращает процент выполнения корневого контекста
     *
     * @return Возвращает процент выполнения корневого контекста
     */
    public float totalProgress() {
        if (parent != null) {
            return parent.totalProgress();
        }
        return percent;
    }

    /**
     * Возвращает текущий процент выполнения
     *
     * @return Возвращает текущий процент выполнения
     */
    public float currentProgress() {
        return percent;
    }

    /**
     * Выполняет обновление процента выполнения текущий и конревой задачи
     *
     * @param child процент выполнения вспомогательной задачи
     */
    private void nextStep(float child) {
        percent = ((float) currentStep + child) / (float) (totalSteps);
        if (parent != null) {
            parent.nextStep(percent);
        }
    }

    /**
     * Увеличивает текущий прогресс выполняемой задачи
     *
     * @return Возвращает контекст выполнения
     */
    public TaskProgressHolder nextStep() {
        currentStep++;
        nextStep(0f);
        return this;
    }
}
