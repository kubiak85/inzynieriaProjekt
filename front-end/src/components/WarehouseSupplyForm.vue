<template>
    <div id="warehouse-supply-form" class="container">
        <h1>Realizacja dostawy</h1>
        <form id="supplier-info" class="container">
            <div class="form-group row">
                <label for="input-firstname" class="col-sm-2 col-form-label">Imię:</label>
                <input
                        type="text"
                        class="form-control"
                        id="input-firstname"
                        placeholder="imię"
                        v-model="supplier.firstname"
                />
            </div>
            <div class="form-group row">
                <label for="input-lastname" class="col-sm-2 col-form-label">Nazwisko:</label>
                <input
                        type="text"
                        class="form-control"
                        id="input-lastname"
                        placeholder="nazwisko"
                        v-model="supplier.lastname"
                />
            </div>
            <div class="form-group row">
                <label for="input-phone" class="col-sm-2 col-form-label">Tel.:</label>
                <input
                        maxlength=9
                        class="form-control"
                        placeholder="000000000"
                        id="input-phone"
                        v-model="supplier.phoneNumber"
                >
            </div>

            <form>
                <div class="row">
                    <div v-if="clicked === false" class="col-sm-6">
                        <select

                                class="form-control"
                                id="input-company"
                                v-model="supplier.company"
                        >
                            <option v-for="(company, index) in companies" v-bind:item="company" v-bind:key="index">
                                {{company}}
                            </option>
                            <option style="color: darkgray" disabled value="">Wybierz firmę</option>
                        </select>
                    </div>
                    <div v-if="clicked === false" class="col-sm-6">
                        <button v-on:click="clicked = true" class="form-control">
                            Dodaj nową firmę
                        </button>
                    </div>
                    <div class="col-sm-4" v-if="clicked === true">
                        <input
                                minlength="3"
                                class="form-control"
                                v-model="company"
                        >
                    </div>
                    <div class="col-sm-4">
                        <button :disabled='(company.length < 3 || company === "")' v-if="clicked === true"
                                v-on:click="addNewCompany()" class="form-control">
                            Zatwierdź
                        </button>
                    </div>
                    <div class="col-sm-4">
                        <button v-if="clicked === true" v-on:click="clicked = false" class="form-control">
                            Wróć
                        </button>
                    </div>
                </div>
            </form>

            <div class="form-group row">
                <label for="input-delivery-date" class="col-sm-2 col-form-label">Data dostawy:</label>
                <input
                        type="date"
                        class="form-control"
                        id="input-delivery-date"
                        v-model="date"
                >
            </div>
            <div class="form-group row">
                <label for="input-delivery-time" class="col-sm-2 col-form-label">Czas dostawy:</label>
                <input
                        type="time"
                        class="form-control"
                        id="input-delivery-time"
                        v-model="time"
                >
            </div>
        </form>
        <table id="supply-products" align="center" class="container">
            <thead>
            <tr>
                <td><strong>Nazwa</strong></td>
                <td><strong>Ilość</strong></td>
                <td><strong>Jednostka</strong></td>
                <td></td>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(product, index) in products" v-bind:item="product" v-bind:key="index">
                <td>
                    <select

                            class="form-control"
                            id="input-product-name"
                            v-model="product.name"
                    >
                        <option :disabled="products.filter(p => (p.name === prod.name)).length > 0"
                                v-for="(prod, index) in allProducts" v-bind:item="product" v-bind:key="index">
                            <strong>
                                {{prod.name}}
                            </strong>
                        </option>
                        <option style="color: darkgray" disabled value="">Wybierz produkt</option>
                    </select>
                </td>
                <td><input id="input-amount" type="number" class="form-control" v-model="product.amount"></td>
                <td>
                    <select
                            class="form-control"
                            type="text"
                            v-model="product.measure"
                    >
                        <option v-for="(measure, index) in measures" v-bind:item="measure" v-bind:key="index">
                            {{measure}}
                        </option>
                        <option style="color: darkgray" disabled value="">jednostka</option>

                    </select>
                </td>
                <td>
                    <button
                            :disabled=
                            "(product.name === '' || product.amount === '' || product.measure === '') &&
                            (index === products.length - 1)"
                            class="form-control" v-if="index === products.length-1" v-on:click="addProduct"
                            style="cursor: pointer">+
                    </button>
                </td>
                <td>
                    <button :disabled="index === 0" class="form-control" v-on:click="removeProduct(index)"
                            v-if="index === products.length-1" style="cursor: pointer">-
                    </button>
                </td>
            </tr>
            </tbody>

        </table>
        <div id="submit-button-div" class="container">
            <button class="form-control" v-on:click="submit">Zatwierdź</button>
        </div>
    </div>
</template>


<script>
    import axios from 'axios';

    export default {
        mounted() {
            document.getElementById('input-phone').addEventListener('keydown', function (e) {
                const regex = RegExp('[0-9]');
                if (!regex.test(e.key) && (e.key != 'Backspace' && e.key != 'Control' && e.key != 'ArrowRight' && e.key != 'ArrowLeft')) {
                    e.preventDefault();
                }
            });
            document.getElementById('input-firstname').addEventListener('keydown', function (e) {
                const regex = RegExp("^[A-Za-z]*$");
                if (!regex.test(e.key) && (e.key != 'Backspace' && e.key != 'Control' && e.key != 'ArrowRight' && e.key != 'ArrowLeft')) {
                    e.preventDefault();
                }
            });
            document.getElementById('input-lastname').addEventListener('keydown', function (e) {
                const regex = RegExp("^[A-Za-z]*$");
                if (!regex.test(e.key) && (e.key != 'Backspace' && e.key != 'Control' && e.key != 'ArrowRight' && e.key != 'ArrowLeft')) {
                    e.preventDefault();
                }
            });
        },
        created() {
            this.getAllCompanies();
            this.getAllMeasures();
            this.getAllProducts();
        },
        data: function () {
            return {
                url: 'http://localhost:8100',
                companies: [],
                measures: Set,
                allProducts: [],
                date: "",
                time: "",
                deliveryDateTime: "",
                supplier: {
                    firstname: "",
                    lastname: "",
                    phoneNumber: "",
                    company: ""
                },
                products: [
                    {
                        productId: "",
                        name: "",
                        measure: "",
                        amount: "",
                        dateTime: ""
                    }
                ],
                clicked: false,
                company: ""
            };
        },
        methods: {

            addNewCompany: function () {
                this.clicked = false;
                let companiesLowerCased = this.companies.slice().map(v => v.toLowerCase());
                if (!companiesLowerCased.includes(this.company.toLowerCase())) {
                    this.companies.push(this.company);
                } else {
                    alert("Taka firma już istnieje na liście");
                }
            },
            getAllProducts: function () {
                axios.get(this.url + '/products/search/findAllByActiveIsTrue?projection=header', {
                    "Access-Control-Allow-Origin": "*",
                    "Content-Type": "application/json"
                }).then((response) => {
                    this.allProducts = response.data._embedded.products;
                    // eslint-disable-next-line no-console
                    console.log(response)
                });
            },
            getAllCompanies: function () {
                axios.get(this.url + '/suppliers/companies', {
                    "Access-Control-Allow-Origin": "*",
                    "Content-Type": "application/json"
                }).then((response) => {
                    this.companies = response.data;
                    // eslint-disable-next-line no-console
                    console.log(response)
                });
            }
            ,
            getAllMeasures: function () {
                axios.get(this.url + '/stock_amounts/measures', {
                    "Access-Control-Allow-Origin": "*",
                    "Content-Type": "application/json"
                }).then((response) => {
                    this.measures = response.data;
                    // eslint-disable-next-line no-console
                    console.log(response)
                });
            },
            addProduct: function () {
                document.createElement('tr');
                this.products.push({
                    productId: "",
                    name: "",
                    measure: "",
                    amount: "",
                    dateTime: "",
                    available: false
                });
                // eslint-disable-next-line no-console
                console.log(this.products);
            },
            removeProduct: function (index) {
                this.products.splice(index, 1);
            },
            submit: async function () {
                if (this.isFormCorrect() == true) {
                    await this.acceptDelivery();
                    window.location.reload(false);
                    this.getAllCompanies();
                }
            },
            isFormCorrect: function () {
                let correct = true;
                let info = [];
                if (this.supplier.company == "") {
                    info.push("niepodana nazwa firmy");
                }
                if (this.date == "") {
                    info.push("niepodana data");
                }
                if (this.time == "") {
                    info.push("niepodany czas");
                }
                if (this.products.length == 1) {
                    info.push("liczba produktów (co najmniej jeden produkt)");
                }
                if (this.supplier.phoneNumber.length !== 9) {
                    info.push("nr telefonu (9 cyfer)");
                }
                if (this.supplier.firstname.length < 3) {
                    info.push("imię (co najmniej 3 litery)");
                }
                if (this.supplier.lastname.length < 3) {
                    info.push("nazwisko (co najmniej 3 litery)");
                }
                if (info.length > 0) {
                    correct = false;
                    let msg = "Niepoprawne dane:\n";
                    for (let i = 0; i < info.length; i++) {
                        msg += info[i] + "\n";
                    }
                    alert(msg);
                }
                return correct;
            },
            acceptDelivery: async function () {
                this.deliveryDateTime = this.date + "T" + this.time + ":00";
                let stockAmounts = this.products.slice();
                let products = this.allProducts.slice();

                stockAmounts.pop();


                for (let i = 0; i < stockAmounts.length; i++) {
                    let productName = stockAmounts[i].name;
                    let productId = products.filter(p => (p.name === productName))[0].id;
                    stockAmounts[i].productId = productId;
                    stockAmounts[i].available = true;
                }

                stockAmounts.forEach(function (item) {
                    delete item.name
                });

                // eslint-disable-next-line no-console
                console.log(stockAmounts)
                const result = await axios.post(this.url + '/supplies/accept_delivery', {
                    supplier: {
                        firstname: this.supplier.firstname,
                        lastname: this.supplier.lastname,
                        company: this.supplier.company,
                        phoneNumber: this.supplier.phoneNumber
                    },
                    deliveryDateTime: this.deliveryDateTime,
                    stockAmounts: stockAmounts
                }, {
                    "Access-Control-Allow-Origin": "*",
                    "Content-Type": "application/json"
                });

                if (result.data === "Saved") {
                    alert("Dodano dostawe");
                } else {
                    alert("Nie dodano dostawy.");
                }
            }
        }
    }
    ;
</script>

<style scoped>
    #supplier-info {
    }

    form > div {
        text-align: left;
    }
</style>