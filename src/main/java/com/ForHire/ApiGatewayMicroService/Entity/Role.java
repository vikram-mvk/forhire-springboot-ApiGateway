package com.ForHire.ApiGatewayMicroService.Entity;

import javax.persistence.*;


@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ENUM_Roles name;

    public Role() { }
    public Role(ENUM_Roles name) { this.name = name;   }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public ENUM_Roles getName() { return name; 	}
    public void setName(ENUM_Roles name) {
        this.name = name;
    }
}
