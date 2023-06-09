package tc.travelCarrier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tc.travelCarrier.domain.Nation;
import tc.travelCarrier.domain.User;

@Repository
public interface NationRepository extends JpaRepository<Nation, Integer> {

    public Nation findNationById(int userId);
}
