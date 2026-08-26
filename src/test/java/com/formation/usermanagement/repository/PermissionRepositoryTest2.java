package com.formation.usermanagement.repository;

import com.formation.usermanagement.config.AuditConfig;
import com.formation.usermanagement.entity.Permission;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
public class PermissionRepositoryTest2 {
    @Autowired
    private PermissionRepository permissionRepository;
    private Permission permissionRead;
    private Permission permissionWrite;
    private Permission permissionDelete;
    private Permission permissionActivate;
    private Permission permissionExpire;
    private Permission permissionLock;
    private Permission permissionRoleAssign;
    private Permission permissionPermissionAssign;

    @BeforeEach
    void setup() {
        permissionRead = permissionRepository.save(
                new Permission("USER", "USER_READ", "Consulter les utilisateurs")
        );
        permissionWrite = permissionRepository.save(
                new Permission("USER", "USER_WRITE", "Créer et modifier des utilisateurs")
        );
        permissionDelete = permissionRepository.save(
                new Permission("USER", "USER_DELETE", "Supprimer des utilisateurs")
        );
        permissionActivate = permissionRepository.save(
                new Permission("USER", "USER_ACTIVATE", "Activer ou désactiver un compte")
        );
        permissionExpire = permissionRepository.save(
                new Permission("USER", "USER_EXPIRE", "Expirer ou renouveler un compte")
        );
        permissionLock = permissionRepository.save(
                new Permission("USER", "USER_LOCK", "Verrouiller ou déverrouiller un compte")
        );
        permissionRoleAssign = permissionRepository.save(
                new Permission("ROLE", "ROLE_ASSIGN", "Assigner ou retirer des rôles")
        );
        permissionPermissionAssign = permissionRepository.save(
                new Permission("PERMISSION", "PERMISSION_ASSIGN", "Ajouter ou retirer des permissions")
        );


    }

    @Test
    public void findByNamepermission() {
        Optional<Permission> found = permissionRepository.findByName("USER_READ");
        assertThat(found).isPresent();

        assertThat(found.get().getCategory().equals("USER"));
        assertThat(found.get().getAuthority().equals("USER_READ"));
        assertThat(found.get().getName().equals("USER_READ"));
        assertThat(found.get().getDescription().equals("consulter users"));


    }

    @Test
    void findByName_DevraitRetournerVide_QuandPermissionNExistePas() {
        Optional<Permission> found = permissionRepository.findByName("PEMISSION_EXISTE");
        assertThat(found).isEmpty();


    }

    @Test
    void existsByName_retouneTrue_si_existe() {
        boolean exist = permissionRepository.existsByName("USER_READ");
        assertThat(exist).isTrue();
    }

    @Test
    void existsByName_retouneFalse_si_nexiste_pas() {
        boolean exist = permissionRepository.existsByName("PERMISSION_NEXISTE");
        assertThat(exist).isFalse();
    }

    @Test
    void existsByCategoryAndName_rtourne_true_si_existe() {
        boolean existByNameAndCat = permissionRepository.existsByCategoryAndName("USER", "USER_READ");
        assertThat(existByNameAndCat);

    }

    @Test
    void existsByCategoryAndName_rtourne_false_combinaison_nexiste_pas() {
        boolean existByNameAndCat = permissionRepository.existsByCategoryAndName("USER", "USER_PER");
        assertThat(existByNameAndCat);

    }

    @Test
    void findByCategory_retourne_listByCat() {
        List<Permission> permissions = permissionRepository.findByCategory("USER");
        assertThat(permissions).hasSize(6);
        assertThat(permissions).extracting("name").contains("USER_READ","USER_WRITE","USER_DELETE");

    }
    @Test
    void findByCategory_retourne_vide_if_per_nexiste_pas() {
        List<Permission> permissions = permissionRepository.findByCategory("CATE_Nex");
        assertThat(permissions).isEmpty();

    }
    @Test
    void findByCategoryOrderByNameAsc_retourne_list_ordonne(){
        List<Permission> odreByName=permissionRepository.findByCategoryOrderByNameAsc("USER");
        assertThat(odreByName.get(0).getName().equals("USER_DELETE"));
        assertThat(odreByName.get(1).getName().equals("USER_READ"));
        assertThat(odreByName.get(2).getName().equals("USER_WRITE"));
    }
    @Test

    void findAllByOrderByCategoryAscNameAsc_liste_triee_by_category_and_name(){
        List<Permission> permissions=permissionRepository.findAllByOrderByCategoryAscNameAsc();
        assertThat(permissions.get(0).getName()).isEqualTo("PERMISSION_ASSIGN");
        assertThat(permissions.get(0).getCategory()).isEqualTo("PERMISSION");
        assertThat(permissions.get(1).getName()).isEqualTo("ROLE_ASSIGN");
        assertThat(permissions.get(1).getCategory()).isEqualTo("ROLE");
        assertThat(permissions.get(2).getCategory()).isEqualTo("USER");
        assertThat(permissions.get(2).getName()).isEqualTo("USER_ACTIVATE");
        assertThat(permissions.get(3).getCategory()).isEqualTo("USER");
        assertThat(permissions.get(3).getName()).isEqualTo("USER_DELETE");
        assertThat(permissions.get(4).getCategory()).isEqualTo("USER");
        assertThat(permissions.get(4).getName()).isEqualTo("USER_EXPIRE");
    }
    @Test
    void countPermissionsByCategory_retourne_nbr_permission(){
        List<Object[]> countPer=permissionRepository.countPermissionsByCategory();
        assertThat(countPer).hasSize(3);
        for (Object[] row:countPer){
            String category= (String) row[0];
            Long count = (Long) row[1];
            if (category.equals("USER")){
                assertThat(count).isEqualTo(6);
            } else if (category.equals("ROLE")){
                assertThat(count).isEqualTo(1);
            }else{
                assertThat(count).isEqualTo(1);
            }
        }

    }

}
