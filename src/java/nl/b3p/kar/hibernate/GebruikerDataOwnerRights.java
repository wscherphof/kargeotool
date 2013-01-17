package nl.b3p.kar.hibernate;

import java.io.Serializable;
import javax.persistence.*;
import nl.b3p.transmodel.DataOwner;

@Entity
public class GebruikerDataOwnerRights implements Serializable {
    @Id
    @ManyToOne
    private Gebruiker gebruiker;
    
    @Id
    @ManyToOne
    private DataOwner dataOwner;
    
    private boolean editable;
    private boolean validatable;

    public Gebruiker getGebruiker() {
        return gebruiker;
    }

    public void setGebruiker(Gebruiker gebruiker) {
        this.gebruiker = gebruiker;
    }

    public DataOwner getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(DataOwner dataOwner) {
        this.dataOwner = dataOwner;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isValidatable() {
        return validatable;
    }

    public void setValidatable(boolean validatable) {
        this.validatable = validatable;
    }
}
