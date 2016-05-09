package scaffold.crud.dao;

import org.seasar.doma.*;
import scaffold.crud.DomaConfig;
import scaffold.crud.entity.User;

import java.util.List;

/**
 * @author kawasima
 */
@Dao(config = DomaConfig.class)
public interface UserDao {
    @Select
    User selectById(Long id);

    @Select
    List<User> selectAll();

    @Insert
    int insert(User user);

    @Update
    int update(User user);

    @Delete
    int delete(User user);

}
