package service;


import entities.Groups;
import entities.User;
import entities.GroupDTO;
import entities.configStructure.Base;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Transactional
public class DataBaseService {
    private static final Logger LOGGER = Logger.getLogger(DataBaseService.class);
    SessionFactory factory = new Configuration() //удалить
            .configure("hibernate.cfg.xml")
            .addAnnotatedClass(Base.class)
            .addAnnotatedClass(Groups.class)
            .buildSessionFactory();
    Session session = null;


    public void addToBase(Base baseElement, String group) {
        session = factory.getCurrentSession();
        session.beginTransaction();
        Groups groups = (Groups) session
                .createQuery("from Groups p where p.name = :name")
                .setParameter("name", group)
                .getSingleResult();
        groups.getBaseElements().add(baseElement);
        session.getTransaction().commit();
    }

    public List<String> getGroups() {
        session = factory.getCurrentSession();
        session.beginTransaction();
        List<String> groups = castList(Groups.class, session.createQuery("from Groups")
                .getResultList())
                .stream()
                .map(Groups::getName)
                .collect(Collectors.toList());
        session.getTransaction().commit();
        return groups;
    }

    public void addUser(User userName, String groupName) {
        session = factory.getCurrentSession();
        session.beginTransaction();
//        User user = new User();
        Groups group = (Groups) session.createQuery("from Groups where name = :name")
                .setParameter("name", groupName)
                .getSingleResult();
//        user.setName(userName);
        userName.setGroups(group);
        session.save(userName);
        session.getTransaction().commit();
    }

    public void addGroup(String title) {
        session = factory.getCurrentSession();
        session.beginTransaction();
        Groups groups = new Groups();
        groups.setName(title);
        session.save(groups);
        session.getTransaction().commit();
    }

    public int doesTheGroupExist(String title) {
        session = factory.getCurrentSession();
        session.beginTransaction();
        try {
            session.createQuery("from Groups where name = :name")
                    .setParameter("name", title)
                    .getSingleResult();
        } catch (NoResultException e) {
            session.getTransaction().commit();
            return -1;
        }
        session.getTransaction().commit();
        return 1;
    }

    public int renameGroup(String oldName, String newName) {
        session = factory.getCurrentSession();
        session.beginTransaction();
        Groups group = (Groups) session.createQuery("from Groups where name = :name")
                .setParameter("name", oldName)
                .getSingleResult();
        group.setName(newName);
        session.save(group);
        session.getTransaction().commit();

        return 1;
    }

    public void deleteGroup(String title) {
        session = factory.getCurrentSession();
        session.beginTransaction();
        Groups groups = (Groups) session.createQuery("from Groups where name = :name")
                .setParameter("name", title)
                .getSingleResult();
        session.delete(groups);
        session.getTransaction().commit();
    }

    public <T> List<T> castList(Class<? extends T> clazz, Collection<?> rawCollection) {
        try {
            return rawCollection.stream().map((Function<Object, T>) clazz::cast).collect(Collectors.toList());
        } catch (ClassCastException e) {
            LOGGER.error(String.format("Преобразование элемента не выполнено. Ошибка: %s", e));
            return Collections.emptyList();
        }
    }

    public List<String> getUserList() {
        session = factory.getCurrentSession();
        session.beginTransaction();
        List<User> userList = castList(User.class, session.createQuery("from User").getResultList());
        session.getTransaction().commit();
        return userList.stream().map(new Function<User, String>() {
            @Override
            public String apply(User user) {
                return String.format("[%s]: %s", user.getGroups().getName(), user.getName()); //необходимо переделать зависимость на OneToMany иначе всё сложно
            }
        }).collect(Collectors.toList());
    }

    public GroupDTO getUsersListByGroup(String groupName) {
        session = factory.getCurrentSession();
        session.beginTransaction();
        List<Groups> list = castList(Groups.class, session.createQuery("from Groups p where p.name = :name")
                .setParameter("name", groupName)
                .getResultList());
        session.getTransaction().commit();
        return new GroupDTO().getGroupDTO(list.get(0));
    }

    public void deleteUserFromBase(User user) {
        session = factory.getCurrentSession();
        session.beginTransaction();

        session.delete(user);
        session.getTransaction().commit();
    }
}
