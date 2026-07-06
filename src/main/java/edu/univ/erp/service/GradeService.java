package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.AssessmentDao;
import edu.univ.erp.data.EnrollmentDao;
import edu.univ.erp.data.GradeDao;
import edu.univ.erp.data.SectionDao;
import edu.univ.erp.domain.Assessment;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Instructor-facing actions: see my sections, manage the assessment scheme, enter scores, compute
 * finals and view class stats. Instructors can only ever touch sections they are assigned to -
 * every method loads the section and calls {@link AccessControl#ensureOwnsSection}.
 */
public class GradeService {

    private final SectionDao sectionDao;
    private final EnrollmentDao enrollmentDao;
    private final AssessmentDao assessmentDao;
    private final GradeDao gradeDao;
    private final AccessControl access;

    public GradeService() {
        this(new SectionDao(), new EnrollmentDao(), new AssessmentDao(), new GradeDao(), new AccessControl());
    }

    public GradeService(SectionDao sectionDao, EnrollmentDao enrollmentDao, AssessmentDao assessmentDao,
                        GradeDao gradeDao, AccessControl access) {
        this.sectionDao = sectionDao;
        this.enrollmentDao = enrollmentDao;
        this.assessmentDao = assessmentDao;
        this.gradeDao = gradeDao;
        this.access = access;
    }

    public List<Section> mySections(User instructor) {
        access.ensureInstructor(instructor);
        return sectionDao.findByInstructor(instructor.getUserId());
    }

    public List<Enrollment> roster(User instructor, int sectionId) {
        Section section = ownSection(instructor, sectionId);
        return enrollmentDao.findBySection(section.getId());
    }

    public List<Assessment> assessments(User instructor, int sectionId) {
        ownSection(instructor, sectionId);
        return assessmentDao.findBySection(sectionId);
    }

    /** Define a new assessment component (e.g. "Quiz 2", weight 10, out of 20). */
    public void addAssessment(User instructor, int sectionId, String name, double weight, double maxScore) {
        access.ensureWritable(instructor);
        ownSection(instructor, sectionId);
        if (name == null || name.isBlank()) {
            throw new ServiceException("Assessment name is required.");
        }
        if (weight <= 0 || maxScore <= 0) {
            throw new ServiceException("Weight and max score must be greater than zero.");
        }
        Assessment a = new Assessment(0, sectionId, name.trim(), weight, maxScore);
        assessmentDao.insert(a);
    }

    /** Enter (or update) one student's score on one assessment. */
    public void enterScore(User instructor, int sectionId, int enrollmentId, int assessmentId, double score) {
        access.ensureWritable(instructor);
        ownSection(instructor, sectionId);

        Assessment assessment = findAssessment(sectionId, assessmentId);
        Enrollment enrollment = enrollmentDao.findById(enrollmentId)
                .orElseThrow(() -> new ServiceException("Enrollment not found."));
        if (enrollment.getSectionId() != sectionId) {
            throw new ServiceException("That student is not in this section.");
        }
        if (score < 0 || score > assessment.getMaxScore()) {
            throw new ServiceException("Score must be between 0 and " + assessment.getMaxScore() + ".");
        }
        gradeDao.upsert(enrollmentId, assessmentId, score);
    }

    /**
     * Compute the final grade for every student in the section using the weighting rule:
     * final = sum over assessments of (score / maxScore) * weight. A missing score counts as 0.
     * Returns how many finals were written.
     */
    public int computeFinals(User instructor, int sectionId) {
        access.ensureWritable(instructor);
        ownSection(instructor, sectionId);

        List<Assessment> scheme = assessmentDao.findBySection(sectionId);
        if (scheme.isEmpty()) {
            throw new ServiceException("Define at least one assessment before computing finals.");
        }
        List<Enrollment> roster = enrollmentDao.findBySection(sectionId);
        for (Enrollment e : roster) {
            double finalGrade = weightedFinal(scheme, gradeDao.findByEnrollment(e.getId()));
            enrollmentDao.updateFinalGrade(e.getId(), finalGrade);
        }
        return roster.size();
    }

    public ClassStats classStats(User instructor, int sectionId) {
        ownSection(instructor, sectionId);
        List<Enrollment> roster = enrollmentDao.findBySection(sectionId);

        double sum = 0, highest = Double.NEGATIVE_INFINITY, lowest = Double.POSITIVE_INFINITY;
        int graded = 0;
        for (Enrollment e : roster) {
            if (e.getFinalGrade() != null) {
                double g = e.getFinalGrade();
                sum += g;
                highest = Math.max(highest, g);
                lowest = Math.min(lowest, g);
                graded++;
            }
        }
        if (graded == 0) {
            return ClassStats.empty();
        }
        return new ClassStats(graded, round2(sum / graded), round2(highest), round2(lowest));
    }

    // --- helpers ----------------------------------------------------------

    private double weightedFinal(List<Assessment> scheme, List<Grade> grades) {
        Map<Integer, Double> scoreByAssessment = new HashMap<>();
        for (Grade g : grades) {
            scoreByAssessment.put(g.getAssessmentId(), g.getScore());
        }
        double total = 0;
        for (Assessment a : scheme) {
            double score = scoreByAssessment.getOrDefault(a.getId(), 0.0);
            total += (score / a.getMaxScore()) * a.getWeight();
        }
        return round2(total);
    }

    private Assessment findAssessment(int sectionId, int assessmentId) {
        return assessmentDao.findBySection(sectionId).stream()
                .filter(a -> a.getId() == assessmentId)
                .findFirst()
                .orElseThrow(() -> new ServiceException("That assessment doesn't belong to this section."));
    }

    /** Load the section and confirm the instructor owns it; used by every method above. */
    private Section ownSection(User instructor, int sectionId) {
        access.ensureInstructor(instructor);
        Section section = sectionDao.findById(sectionId)
                .orElseThrow(() -> new ServiceException("Section not found."));
        access.ensureOwnsSection(instructor, section);
        return section;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
