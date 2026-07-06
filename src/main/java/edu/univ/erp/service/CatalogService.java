package edu.univ.erp.service;

import edu.univ.erp.data.CourseDao;
import edu.univ.erp.data.SectionDao;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;

import java.util.List;

/**
 * Read-only views of the catalog that any logged-in user can see: the list of courses and the
 * list of sections (with seats/instructor filled in). Kept separate so both students and admins
 * can reuse it without duplicating queries.
 */
public class CatalogService {

    private final CourseDao courseDao;
    private final SectionDao sectionDao;

    public CatalogService() {
        this(new CourseDao(), new SectionDao());
    }

    public CatalogService(CourseDao courseDao, SectionDao sectionDao) {
        this.courseDao = courseDao;
        this.sectionDao = sectionDao;
    }

    public List<Course> listCourses() {
        return courseDao.findAll();
    }

    public List<Section> listSections() {
        return sectionDao.findAll();
    }

    public Section getSection(int sectionId) {
        return sectionDao.findById(sectionId)
                .orElseThrow(() -> new ServiceException("Section not found."));
    }
}
