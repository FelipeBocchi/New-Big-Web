export class Customer {

    id: number;
    name: String;
    cpf: String;
    phone: String;
    email: String;
    active: Boolean;
    birthDate: Date;

    constructor( id: number,name: String, cpf: String,  phone: String, email: String, active: Boolean, birthDate: Date) {
        this.id = id;
        this.name = name;
        this.cpf = cpf;
        this.phone = phone;
        this.email = email;
        this.active = active;
        this.birthDate = birthDate;
    }

}
