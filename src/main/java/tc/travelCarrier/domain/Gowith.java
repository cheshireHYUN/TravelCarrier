package tc.travelCarrier.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class Gowith {

    public Gowith(){}
    public Gowith(User user){
        this.user = user;
    }
    @Id @GeneratedValue
    @Column(name = "GOWITH_NO")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WEEKLY_ID")
    private Weekly weekly;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;




}
